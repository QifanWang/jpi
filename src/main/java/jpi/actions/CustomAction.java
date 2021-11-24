package jpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import jpi.notifiers.MyNotifier;
import jpi.utility.Util;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CustomAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {

    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project curProj = e.getProject();
        Objects.requireNonNull(curProj);

        MyNotifier.notifyInfo(curProj, "Jpi starts working.");

        StringBuilder sb = new StringBuilder();

        //1) project name
        sb.append(curProj.getName()).append("\n");

        Map<String, List<Path>> eclipseConfig = getEclipseConfig(curProj.getBasePath());

        // source folder of the action file
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if(null == psiFile) {
            sb.append("Actioned Psi file is null.\n");
        } else {
            SourceFolder sf = ProjectFileIndex.SERVICE
                    .getInstance(curProj)
                    .getSourceFolder(psiFile.getVirtualFile());

            sb.append(sf == null
                    ?"Source folder of the actioned Psi file is null.\n"
                    :"Source folder of the actioned Psi file is " + Util.rmHeader(sf.getUrl()) + "\n");
        }

        Module[] modules = ModuleManager.getInstance(curProj).getModules();
        for(Module m : modules) {
            sb.append("---------\n");
            sb.append("Module ").append(m.getName()).append("\n");

            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(m);

            //2) source root path
            sb.append("source code directory paths: \n");
            String[] srUrls = moduleRootManager
                    .getSourceRootUrls();
            for(String url : srUrls) {
                sb.append(Util.rmHeader(url)).append("\n");
            }

            //3) output paths
            String outputUrl = Objects.requireNonNull(CompilerProjectExtension.getInstance(curProj)).getCompilerOutputUrl();
            sb.append("output directory: \n");
            // eclipse output
            if(eclipseConfig.containsKey("output")) {
                sb.append(eclipseConfig.get("output").toString().replace('\\', '/'))
                  .append(';');
            }
            // IDEA output
            sb.append(Util.rmHeader(outputUrl)).append("\n");

            //4) dependency lib
            sb.append("libraries: \n");
            VirtualFile[] roots = moduleRootManager.orderEntries().classes().getRoots();
            for(VirtualFile r : roots) {
                sb.append(Util.rmHeader(r.getUrl())).append("\n");
            }

            //5) a list of modules on which this cur mod depends on
//            Module[] dependentModules = moduleRootManager.getDependencies();
            String[] dependentModulesNames = moduleRootManager.getDependencyModuleNames();
            sb.append("dependent modules: \n");
            sb.append(Arrays.toString(dependentModulesNames));
        }

        Util.writeOutput(curProj.getBasePath(), sb);

        MyNotifier.notifyInfo(curProj, "Jpi outputs into jpi-output.txt file.\nPlease check.");
    }

    private static Map<String, List<Path>> getEclipseConfig(String basePath) {
        Map<String, List<Path>> kind2paths = new HashMap<>();
        File classPath = new File(basePath + "/.classpath");

        if(!classPath.exists()) return kind2paths;

        try
        {

            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            InputStream inputStream= new FileInputStream(classPath);

            Document doc = dBuilder.parse(inputStream);

            doc.getDocumentElement().normalize();

            // just getElementsByTagName not use getDocumentElement().getElementsByTagName
            NodeList list = doc.getElementsByTagName("classpathentry");
            final int N = list.getLength();
            for(int i = 0; i < N; ++i) {
                NamedNodeMap nameMap = list.item(i).getAttributes();
                if(nameMap == null) continue;

                Node kind = nameMap.getNamedItem("kind");
                if(kind == null) continue;

                switch (kind.getNodeValue()) {
                    case "output":
                        List<Path> dirs = kind2paths.getOrDefault(kind.getNodeValue(), new ArrayList<>());
                        dirs.add(Paths.get(basePath, nameMap.getNamedItem("path").getNodeValue()));
                        kind2paths.putIfAbsent(kind.getNodeValue(), dirs);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return kind2paths;
    }
}
