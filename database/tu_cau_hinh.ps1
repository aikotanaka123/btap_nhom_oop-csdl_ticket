# Script TU CAU HINH SQL Server cho ung dung Dat ve xem phim. Chay lai nhieu lan van an toan.
# Dung cho CA 2 cach chay (PowerShell "Run as Administrator"):
#  - Chay bang Maven/IDE (khong tham so):
#        powershell -ExecutionPolicy Bypass -File database\tu_cau_hinh.ps1
#    -> login datve_app, ghi src\main\resources\db.properties
#  - Ban exe: DatVeXemPhim.exe tu goi script nay kem -ConfigPath va -SqlDir
#    -> login datve_exe, ghi %APPDATA%\DatVeXemPhim\db.properties
# Cac buoc:
#  1. Kiem tra da cai SQL Server (instance MSSQLSERVER) va sqlcmd; bat dich vu neu dang tat.
#  2. Bat TCP/IP cong 1433 (chi khoi dong lai dich vu khi can thay doi).
#  3. Neu CHUA co database DatVeXemPhim: chay tao_csdl.sql.
#     Da co database thi KHONG chay lai (tao_csdl.sql se xoa het du lieu).
#  4. Tao/doi mat khau login SQL.
#  5. Ghi file cau hinh (app.mode=jdbc) va thu ket noi qua TCP.
# Nhat ky: cau_hinh.log canh file cau hinh (ban exe) hoac %TEMP%\datve_cau_hinh.log (ban Maven).
param(
    [string] $ConfigPath = '',
    [string] $SqlDir = $PSScriptRoot,
    [string] $Login = ''
)
$cheDoMaven = ($ConfigPath -eq '')
if ($cheDoMaven) {
    $ConfigPath = Join-Path $PSScriptRoot '..\src\main\resources\db.properties'
    if ($Login -eq '') { $Login = 'datve_app' }
} elseif ($Login -eq '') {
    $Login = 'datve_exe'
}
$ErrorActionPreference = 'Stop'
$thuMucCauHinh = Split-Path -Parent $ConfigPath
New-Item -ItemType Directory -Force -Path $thuMucCauHinh | Out-Null
$log = if ($cheDoMaven) { Join-Path $env:TEMP 'datve_cau_hinh.log' } else { Join-Path $thuMucCauHinh 'cau_hinh.log' }
Start-Transcript -Path $log -Force | Out-Null

function Invoke-Sql([string] $query) {
    $out = & sqlcmd -S localhost -E -C -b -h -1 -W -Q "SET NOCOUNT ON; $query" 2>&1
    if ($LASTEXITCODE -ne 0) { throw "sqlcmd loi: $out" }
    return ($out | Out-String).Trim()
}

$maLoi = 0
try {
    Write-Host '=== Tu cau hinh SQL Server cho ung dung Dat ve xem phim ==='

    # 1. SQL Server va sqlcmd
    if (-not (Get-Command sqlcmd -ErrorAction SilentlyContinue)) {
        throw 'Khong tim thay sqlcmd. Hay cai SQL Server / SSMS (kem Command Line Utilities).'
    }
    $instanceId = (Get-ItemProperty 'HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\Instance Names\SQL' -ErrorAction SilentlyContinue).MSSQLSERVER
    if (-not $instanceId) {
        throw 'Chua cai SQL Server voi instance mac dinh MSSQLSERVER.'
    }
    $dichVu = Get-Service MSSQLSERVER
    if ($dichVu.Status -ne 'Running') {
        Write-Host 'Dang bat dich vu SQL Server...'
        Set-Service MSSQLSERVER -StartupType Automatic
        Start-Service MSSQLSERVER
    }

    # 2. TCP/IP cong 1433 + cho phep dang nhap bang tai khoan SQL (mixed mode, LoginMode = 2)
    $goc = "HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\$instanceId\MSSQLServer"
    $tcp = "$goc\SuperSocketNetLib\Tcp"
    $ipAll = Get-ItemProperty "$tcp\IPAll"
    $canKhoiDongLai = $false
    if ((Get-ItemProperty $tcp).Enabled -ne 1 -or $ipAll.TcpPort -ne '1433' -or $ipAll.TcpDynamicPorts -ne '') {
        Write-Host 'Bat TCP/IP cong 1433...'
        Set-ItemProperty $tcp -Name Enabled -Value 1
        Set-ItemProperty "$tcp\IPAll" -Name TcpPort -Value '1433'
        Set-ItemProperty "$tcp\IPAll" -Name TcpDynamicPorts -Value ''
        $canKhoiDongLai = $true
    }
    if ((Get-ItemProperty $goc).LoginMode -ne 2) {
        Write-Host 'Bat SQL Server Authentication (mixed mode)...'
        Set-ItemProperty $goc -Name LoginMode -Value 2
        $canKhoiDongLai = $true
    }
    if ($canKhoiDongLai) {
        Write-Host 'Khoi dong lai SQL Server...'
        Restart-Service MSSQLSERVER -Force
    } else {
        Write-Host 'TCP/IP 1433 va SQL Server Authentication da bat san.'
    }

    # Cho SQL Server san sang (toi da ~60 giay)
    $sanSang = $false
    for ($i = 0; $i -lt 30 -and -not $sanSang; $i++) {
        & sqlcmd -S localhost -E -C -b -Q 'SELECT 1' *> $null
        if ($LASTEXITCODE -eq 0) { $sanSang = $true } else { Start-Sleep -Seconds 2 }
    }
    if (-not $sanSang) { throw 'SQL Server khong phan hoi sau khi khoi dong.' }

    # 3. Database
    if ((Invoke-Sql "SELECT ISNULL(CAST(DB_ID('DatVeXemPhim') AS VARCHAR(10)), 'KHONG')") -eq 'KHONG') {
        foreach ($f in @('tao_csdl.sql')) {
            $duongDan = Join-Path $SqlDir $f
            if (-not (Test-Path $duongDan)) { throw "Thieu file $duongDan" }
            Write-Host "Dang chay $f ..."
            & sqlcmd -S localhost -E -C -b -f 65001 -i $duongDan
            if ($LASTEXITCODE -ne 0) { throw "Chay $f that bai." }
        }
    } else {
        Write-Host 'Database DatVeXemPhim da co san - giu nguyen du lieu.'
    }

    # 4. Login SQL
    $chu = (48..57) + (65..90) + (97..122)
    $pw = 'DatVe@' + (-join ($chu | Get-Random -Count 12 | ForEach-Object { [char]$_ }))
    Invoke-Sql @"
IF SUSER_ID('$Login') IS NULL
    CREATE LOGIN [$Login] WITH PASSWORD = '$pw', CHECK_POLICY = OFF, DEFAULT_DATABASE = DatVeXemPhim;
ELSE
    ALTER LOGIN [$Login] WITH PASSWORD = '$pw';
USE DatVeXemPhim;
IF USER_ID('$Login') IS NULL CREATE USER [$Login] FOR LOGIN [$Login];
ALTER ROLE db_owner ADD MEMBER [$Login];
"@ | Out-Null
    Write-Host "Da tao/cap nhat login $Login."

    # 5. File cau hinh + thu ket noi TCP
    @"
# Tao tu dong boi database/tu_cau_hinh.ps1 - chua mat khau, KHONG commit/chia se file nay
app.mode=jdbc
db.url=jdbc:sqlserver://localhost:1433;databaseName=DatVeXemPhim;encrypt=true;trustServerCertificate=true
db.user=$Login
db.password=$pw
"@ | Set-Content -Encoding ascii $ConfigPath

    & sqlcmd -S 'tcp:localhost,1433' -U $Login -P $pw -C -b -Q 'SELECT DB_NAME() AS KetNoiTCP_OK'
    if ($LASTEXITCODE -ne 0) { throw 'Thu ket noi TCP cong 1433 that bai (kiem tra tuong lua / SQL Server Authentication).' }
    Write-Host "XONG. Da ghi cau hinh: $ConfigPath"
}
catch {
    Write-Host "LOI: $($_.Exception.Message)"
    $maLoi = 1
}
finally {
    Stop-Transcript | Out-Null
}
if ($maLoi -ne 0) {
    Write-Host 'Nhan Enter de dong cua so...'
    [void][Console]::ReadLine()
}
exit $maLoi
