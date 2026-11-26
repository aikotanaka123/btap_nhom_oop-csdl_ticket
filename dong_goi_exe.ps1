# Dong goi ung dung thanh file run.exe o thu muc goc du an.
# Chay tai thu muc goc du an:   powershell -ExecutionPolicy Bypass -File dong_goi_exe.ps1
#
# run.exe la 1 file duy nhat (da nhung san DatVeXemPhim.jar + database\tao_csdl.sql + tu_cau_hinh.ps1).
# Bam dup run.exe:
#   1. giai nen app vao %LOCALAPPDATA%\DatVeXemPhim\run (chi ghi lai khi ban moi khac ban cu);
#   2. tim Java 17+ tren may (JAVA_HOME, PATH, registry, Program Files);
#   3. mo app o che do JDBC: lan dau (hoac khi mat ket noi) app TU cau hinh SQL Server
#      (TCP 1433, tao database neu chua co, login datve_exe) - Windows chi hoi quyen Admin (UAC);
#      cau hinh luu o %APPDATA%\DatVeXemPhim\db.properties, lan sau vao thang.
# May chay run.exe can: Java 17+ va SQL Server (instance MSSQLSERVER) kem sqlcmd.
#
# Them -KemJava de tao them ban dist\DatVeXemPhim\DatVeXemPhim.exe kem san Java (jpackage, ~270 file).
param([switch] $KemJava)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$csc = Join-Path $env:WINDIR 'Microsoft.NET\Framework64\v4.0.30319\csc.exe'
if (-not (Test-Path $csc)) { $csc = Join-Path $env:WINDIR 'Microsoft.NET\Framework\v4.0.30319\csc.exe' }
if (-not (Test-Path $csc)) { throw 'Khong tim thay csc.exe (.NET Framework 4) de tao run.exe.' }

Write-Host '1/4 Build jar (mvn package)...'
& mvn -q package -DskipTests
if ($LASTEXITCODE -ne 0) { throw 'mvn package that bai.' }

Write-Host '2/4 Chuan bi file nhung vao exe...'
$thuMucVao = Join-Path $PSScriptRoot 'target\exe-input'
if (Test-Path $thuMucVao) { Remove-Item -Recurse -Force $thuMucVao }
New-Item -ItemType Directory -Force -Path "$thuMucVao\database" | Out-Null
$jar = Join-Path $thuMucVao 'DatVeXemPhim.jar'
Copy-Item 'target\datvexemphim-1.0-SNAPSHOT.jar' $jar

# Bo db.properties (chua mat khau cua may nay) ra khoi jar: ban exe tu cau hinh rieng tren tung may.
Add-Type -AssemblyName System.IO.Compression, System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::Open($jar, 'Update')
try {
    $entry = $zip.GetEntry('db.properties')
    if ($entry) { $entry.Delete() }
} finally { $zip.Dispose() }

Copy-Item 'database\tao_csdl.sql', 'database\tu_cau_hinh.ps1' "$thuMucVao\database"

# Icon cho run.exe (ve bang code, khong can file anh rieng)
Add-Type -AssemblyName System.Drawing
$bmp = New-Object System.Drawing.Bitmap 64, 64
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = 'AntiAlias'
$g.Clear([System.Drawing.Color]::Transparent)
$g.FillEllipse((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(183, 28, 28))), 2, 2, 60, 60)
$g.FillPolygon([System.Drawing.Brushes]::White, [System.Drawing.Point[]]@(
    (New-Object System.Drawing.Point 24, 16), (New-Object System.Drawing.Point 24, 48), (New-Object System.Drawing.Point 50, 32)))
$g.Dispose()
$icon = [System.Drawing.Icon]::FromHandle($bmp.GetHicon())
$fileIcon = Join-Path $thuMucVao 'run.ico'
$fs = [System.IO.File]::Create($fileIcon); $icon.Save($fs); $fs.Close()
$bmp.Dispose()

Write-Host '3/4 Tao run.exe...'
$nguon = Join-Path $thuMucVao 'run.cs'
@'
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Security.Cryptography;
using System.Windows.Forms;
using Microsoft.Win32;

[assembly: AssemblyTitle("Dat ve xem phim")]
[assembly: AssemblyProduct("DatVeXemPhim")]

static class Run {
    const string TIEU_DE = "Đặt vé xem phim";

    [STAThread]
    static int Main() {
        try {
            string appDir = Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.LocalApplicationData), "DatVeXemPhim", "run");
            Directory.CreateDirectory(Path.Combine(appDir, "database"));
            string jar = Path.Combine(appDir, "DatVeXemPhim.jar");
            GiaiNen("DatVeXemPhim.jar", jar);
            GiaiNen("tao_csdl.sql", Path.Combine(appDir, "database", "tao_csdl.sql"));
            GiaiNen("tu_cau_hinh.ps1", Path.Combine(appDir, "database", "tu_cau_hinh.ps1"));

            string java = TimJava();
            if (java == null) {
                MessageBox.Show("Không tìm thấy Java 17 trở lên trên máy này.\n\n"
                    + "Hãy cài JDK 17+ (vd Eclipse Temurin) rồi mở lại run.exe.",
                    TIEU_DE, MessageBoxButtons.OK, MessageBoxIcon.Error);
                return 1;
            }
            ProcessStartInfo psi = new ProcessStartInfo(java,
                "-Ddatve.exe=true -Ddatve.tudong=true \"-Ddatve.appdir=" + appDir + "\" -jar \"" + jar + "\"");
            psi.UseShellExecute = false;
            psi.WorkingDirectory = appDir;
            Process.Start(psi);
            return 0;
        } catch (Exception e) {
            MessageBox.Show("Không mở được ứng dụng:\n" + e.Message, TIEU_DE,
                MessageBoxButtons.OK, MessageBoxIcon.Error);
            return 1;
        }
    }

    /** Ghi file nhung trong exe ra dia, bo qua neu file tren dia da giong het. */
    static void GiaiNen(string ten, string dich) {
        byte[] moi;
        using (Stream s = Assembly.GetExecutingAssembly().GetManifestResourceStream(ten))
        using (MemoryStream m = new MemoryStream()) {
            s.CopyTo(m);
            moi = m.ToArray();
        }
        if (File.Exists(dich)) {
            byte[] cu = File.ReadAllBytes(dich);
            if (cu.Length == moi.Length && Bam(cu) == Bam(moi)) return;
        }
        try {
            File.WriteAllBytes(dich, moi);
        } catch (IOException) {
            if (!File.Exists(dich)) throw; // file dang bi app cu khoa -> dung ban dang co
        }
    }

    static string Bam(byte[] b) {
        using (SHA256 h = SHA256.Create()) return Convert.ToBase64String(h.ComputeHash(b));
    }

    /** Tim javaw.exe cua Java >= 17, uu tien ban moi nhat. */
    static string TimJava() {
        List<string> home = new List<string>();
        string jh = Environment.GetEnvironmentVariable("JAVA_HOME");
        if (!string.IsNullOrEmpty(jh)) home.Add(jh);
        string path = Environment.GetEnvironmentVariable("PATH") ?? "";
        foreach (string d in path.Split(';')) {
            try {
                if (d.Trim().Length > 0 && File.Exists(Path.Combine(d.Trim(), "javaw.exe")))
                    home.Add(Path.GetDirectoryName(d.Trim().TrimEnd('\\')));
            } catch (ArgumentException) { }
        }
        foreach (string khoa in new string[] { @"SOFTWARE\JavaSoft\JDK", @"SOFTWARE\JavaSoft\Java Runtime Environment" }) {
            using (RegistryKey k = Registry.LocalMachine.OpenSubKey(khoa)) {
                if (k == null) continue;
                foreach (string ver in k.GetSubKeyNames()) {
                    using (RegistryKey kv = k.OpenSubKey(ver)) {
                        object h = kv == null ? null : kv.GetValue("JavaHome");
                        if (h != null) home.Add(h.ToString());
                    }
                }
            }
        }
        string pf = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);
        foreach (string hang in new string[] { "Java", "Eclipse Adoptium", "Microsoft", "Zulu", "Amazon Corretto", "BellSoft", "Semeru" }) {
            string goc = Path.Combine(pf, hang);
            if (Directory.Exists(goc)) home.AddRange(Directory.GetDirectories(goc));
        }

        string tot = null; int totVer = 0;
        foreach (string h in home) {
            string javaw = Path.Combine(h, "bin", "javaw.exe");
            if (!File.Exists(javaw)) continue;
            int v = PhienBan(h);
            if (v >= 17 && v > totVer) { tot = javaw; totVer = v; }
        }
        return tot;
    }

    /** Doc JAVA_VERSION="17.0.2" trong file release cua thu muc Java. */
    static int PhienBan(string home) {
        try {
            foreach (string dong in File.ReadAllLines(Path.Combine(home, "release"))) {
                if (!dong.StartsWith("JAVA_VERSION=")) continue;
                string v = dong.Substring(13).Trim('"');
                if (v.StartsWith("1.")) v = v.Substring(2);
                int n = 0;
                foreach (char c in v) { if (!char.IsDigit(c)) break; n = n * 10 + (c - '0'); }
                return n;
            }
        } catch (IOException) { } catch (UnauthorizedAccessException) { }
        return 0;
    }
}
'@ | Set-Content -Encoding UTF8 $nguon

$exe = Join-Path $PSScriptRoot 'run.exe'
& $csc /nologo /target:winexe /optimize+ /codepage:65001 "/out:$exe" "/win32icon:$fileIcon" `
    /r:System.Windows.Forms.dll `
    "/resource:$jar,DatVeXemPhim.jar" `
    "/resource:$thuMucVao\database\tao_csdl.sql,tao_csdl.sql" `
    "/resource:$thuMucVao\database\tu_cau_hinh.ps1,tu_cau_hinh.ps1" `
    $nguon
if ($LASTEXITCODE -ne 0) { throw 'Bien dich run.exe that bai (dang mo run.exe? hay dong lai roi chay lai).' }

if ($KemJava) {
    Write-Host '   Tao them dist\DatVeXemPhim (jpackage, mat 1-2 phut)...'
    $jpackage = $null
    if ($env:JAVA_HOME) { $jpackage = Join-Path $env:JAVA_HOME 'bin\jpackage.exe' }
    if (-not $jpackage -or -not (Test-Path $jpackage)) { $jpackage = (Get-Command jpackage -ErrorAction SilentlyContinue).Source }
    if (-not $jpackage) { throw 'Khong tim thay jpackage. Dat JAVA_HOME tro toi JDK 17+.' }
    Remove-Item $nguon, $fileIcon
    $dist = Join-Path $PSScriptRoot 'dist'
    if (Test-Path "$dist\DatVeXemPhim") { Remove-Item -Recurse -Force "$dist\DatVeXemPhim" }
    & $jpackage --type app-image --name DatVeXemPhim --app-version 1.0 --input $thuMucVao `
        --main-jar DatVeXemPhim.jar --main-class com.nhom.datve.Main `
        --java-options '-Ddatve.exe=true' --java-options '-Ddatve.tudong=true' `
        --java-options '-Ddatve.appdir=$APPDIR' --dest $dist
    if ($LASTEXITCODE -ne 0) { throw 'jpackage that bai.' }
}

Write-Host '4/4 Don file build tam trong target\ ...'
Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'target') -Force | Remove-Item -Recurse -Force
Write-Host "XONG: $exe  ($([math]::Round((Get-Item $exe).Length / 1MB, 1)) MB)"
