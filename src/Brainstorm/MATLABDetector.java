
package brainstorm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MATLABDetector {
    public static String detectMATLABPath() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe",
                "($path = Get-ItemPropertyValue -Path 'HKLM:\\SOFTWARE\\MathWorks\\MATLAB' -Name 'MATLABROOT' -ErrorAction SilentlyContinue); " +
                "if (-not $path) { $path = Get-ItemPropertyValue -Path 'HKLM:\\SOFTWARE\\WOW6432Node\\MathWorks\\MATLAB' -Name 'MATLABROOT' -ErrorAction SilentlyContinue }; " +
                "Write-Output $path"
            );
            
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String path = reader.readLine();
            
            if (path != null && !path.trim().isEmpty() && new File(path.trim() + "\\bin\\matlab.exe").exists()) {
                return path.trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}