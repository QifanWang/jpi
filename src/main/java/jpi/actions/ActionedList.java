package jpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import jpi.notifiers.MyNotifier;
import jpi.utility.Util;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;

public class ActionedList extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Project curProj = e.getProject();
        Objects.requireNonNull(curProj);

        MyNotifier.notifyInfo(curProj, "Jpi starts working.");

        StringBuilder sb = new StringBuilder();

        //1) project name
        sb.append("Actioned Psi element belongs to project ").append(curProj.getName()).append("\n");

        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        if(null != element) {
            sb.append("Psi element: ").append(element.toString()).append("\n");

            if(element instanceof PsiDirectory) {
                sb.append("Actioned element is a Directory.\n");

                // recursively get all classes;
                sb.append("Classes: \n");
                Queue<PsiDirectory> dirs = new ArrayDeque<>();
                dirs.add((PsiDirectory) element);
                while(!dirs.isEmpty()) {
                    PsiDirectory cur = dirs.poll();
                    // files
                    PsiFile[] files = cur.getFiles();
                    for (PsiFile f : files) {
                        if (f instanceof PsiJavaFile) {
                            getClassesInFile(sb, (PsiJavaFile) f);
                        }
                    }
                    // sub directories
                    PsiDirectory[] subs = cur.getSubdirectories();
                    dirs.addAll(Arrays.asList(subs));
                }

                sb.append("Containing Directory: ").append(((PsiDirectory) element).getParentDirectory()).append('\n');

            } else if(element instanceof PsiJavaFile) {
                sb.append("Actioned element is a Java File.\n");

                sb.append("FileName: ").append(((PsiJavaFile) element).getName()).append('\n');

                sb.append("Classes: \n");
                getClassesInFile(sb, (PsiJavaFile) element);

                sb.append("Containing Package: ").append(((PsiJavaFile) element).getPackageName()).append('\n');

                sb.append("Containing Directory: ").append(((PsiFile) element).getContainingDirectory()).append('\n');
            } else if(element instanceof PsiClass) {
                sb.append("Actioned element is a Class.\n");

                sb.append("Class Name: ").append(((PsiClass) element).getName()).append('\n');

                PsiFile f = element.getContainingFile();
                sb.append("Containing File: ").append(f).append('\n');

                if(f instanceof PsiJavaFile)
                    sb.append("Containing Package: ").append(((PsiJavaFile) f).getPackageName()).append('\n');
            } else if(element instanceof PsiMethod) {
                sb.append("Actioned element is a Method.\n");

                sb.append("Method Name: ").append(((PsiMethod) element).getName()).append('\n');

                sb.append("Parameter List: ").append(((PsiMethod) element).getParameterList()).append('\n');

                sb.append("Return Type: ").append(((PsiMethod) element).getReturnType()).append('\n');

                sb.append("Containing Class: ").append(((PsiMethod) element).getContainingClass()).append('\n');

                PsiFile f = element.getContainingFile();
                sb.append("Containing File: ").append(f).append('\n');

                if(f instanceof PsiJavaFile)
                    sb.append("Containing Package: ").append(((PsiJavaFile) f).getPackageName()).append('\n');
            } else {
                sb.append("Actioned element has no information\n");
            }
        }
        else
            sb.append("psi element is null\n");


        Util.writeOutput(curProj.getBasePath(), sb);

        MyNotifier.notifyInfo(curProj, "Jpi outputs into jpi-output.txt file.\nPlease check.");
    }


    private void getClassesInFile(@NotNull StringBuilder sb, PsiJavaFile javaFile) {
        PsiClass[]  all = javaFile.getClasses();
        for(PsiClass c : all) {
            sb.append(c.getQualifiedName()).append('\n');
        }
    }
}
