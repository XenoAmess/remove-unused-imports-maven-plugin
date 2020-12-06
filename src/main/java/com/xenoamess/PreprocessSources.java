package com.xenoamess;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * preprocess sources in sourceDirectory.
 */
@Mojo(name = "process", defaultPhase = LifecyclePhase.INITIALIZE)
public class PreprocessSources extends AbstractMojo {

    @Parameter(defaultValue = "${basedir}/target/pmd.xml", property = "pmdXmlPath", required = true)
    private File pmdXmlPath;

    @Parameter(defaultValue = "UnusedImports", property = "ruleNames", required = true)
    private List<String> ruleNames;

    public PreprocessSources() {
    }

    @Override
    public void execute() throws MojoExecutionException {
        boolean haveOtherViolationsThatCannotAutoDelete = false;
        if (pmdXmlPath == null || !pmdXmlPath.exists()) {
            System.out.println("can not find pmd.xml at path:" + pmdXmlPath);
            System.out.println("will do nothing.");
            return;
        }
        String pmdXmlString = null;
        try {
            pmdXmlString = FileUtils.readFileToString(pmdXmlPath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoExecutionException("could not read pmd.xml", e);
        }
        Document document;
        try {
            document = DocumentHelper.parseText(pmdXmlString);
        } catch (DocumentException e) {
            e.printStackTrace();
            throw new MojoExecutionException("could not parse pmd.xml", e);
        }

        for (int i = 0; i < document.nodeCount(); i++) {
            Node rootNode = document.node(i);
            if (rootNode instanceof Element) {
                for (int j = 0; j < ((Element) rootNode).nodeCount(); j++) {
                    Node fileNode = ((Element) rootNode).node(j);
                    if (fileNode instanceof Element && "file".equals(fileNode.getName())) {
                        String filePath = ((Element) fileNode).attributeValue("name");
                        String fileContent;
                        try {
                            File file = new File(filePath);
                            fileContent = FileUtils.readFileToString(file);
                            String[] fileContentLines = fileContent.split("(\r\n)|(\r)|(\n)");
                            boolean ifModified = false;
                            List<Node> removedNodes = new ArrayList<>();
                            for (int k = 0; k < ((Element) fileNode).nodeCount(); k++) {
                                Node violationNode = ((Element) fileNode).node(k);
                                if ("violation".equals(violationNode.getName()) && violationNode instanceof Element && ruleNames.contains(((Element) violationNode).attributeValue("rule"))) {
                                    int beginline = Integer.parseInt(((Element) violationNode)
                                            .attributeValue("beginline")) - 1;
                                    int endline = Integer.parseInt(((Element) violationNode)
                                            .attributeValue("endline")) - 1;
                                    int begincolumn = Integer.parseInt(((Element) violationNode)
                                            .attributeValue("begincolumn")) - 1;
                                    int endcolumn = Integer.parseInt(((Element) violationNode).
                                            attributeValue("endcolumn")) - 1;


                                    for (int l = beginline; l <= endline; l++) {
                                        int currentStart = 0;
                                        int currentEnd = fileContentLines[l].length() - 1;
                                        if (l == beginline) {
                                            currentStart = begincolumn;
                                        }
                                        if (l == endline) {
                                            currentEnd = endcolumn;
                                        }
                                        StringBuilder stringBuilder = new StringBuilder(fileContentLines[l]);
                                        for (int m = currentStart; m <= currentEnd; m++) {
//                                            System.out.println(filePath);
//                                            System.out.println(beginline);
//                                            System.out.println(endline);
//                                            System.out.println(l);
//                                            System.out.println(stringBuilder.toString());
//                                            System.out.println(currentStart);
//                                            System.out.println(currentEnd);
//                                            System.out.println(m);
                                            stringBuilder.setCharAt(m, ' ');
                                        }
                                        fileContentLines[l] = stringBuilder.toString();
                                    }
                                    ifModified = true;
                                    removedNodes.add(violationNode);
                                } else {
                                    haveOtherViolationsThatCannotAutoDelete = true;
                                }
                            }
                            if (ifModified) {
                                String newFileContent = String.join(System.lineSeparator(), fileContentLines);
                                FileUtils.writeStringToFile(file, newFileContent);
                            }
                            for (Node removedNode : removedNodes) {
                                ((Element) fileNode).remove(removedNode);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        try (Writer fileWriter = new FileWriter(pmdXmlPath);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        ) {
            document.write(bufferedWriter);
        } catch (IOException e) {
            System.err.println("cannot save back to pmd.xml because : ");
            e.printStackTrace();
        }

        if (haveOtherViolationsThatCannotAutoDelete) {
            final String newPmdFileContent;
            try {
                newPmdFileContent = FileUtils.readFileToString(pmdXmlPath);
            } catch (IOException e) {
                throw new MojoExecutionException(
                        "still have other pmd violations to solve!!! Also, new pmd file read failed."
                );
            }
            throw new MojoExecutionException(
                    "still have other pmd violations to solve!!! pmd file be : \n" + newPmdFileContent
            );
        }

    }
}
