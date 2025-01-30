package smpBasedMax;

import ij.gui.HTMLDialog;
import ij.plugin.PlugIn;

public class Help_Window implements PlugIn {
    @Override
    public void run(String arg) {
        String HELP_INSTRUCTION = "<h3><strong>THIS IS A HEADING:</strong></h3>\n" +
                "<ul>\n" +
                "    <li>item1</li>\n" +
                "    <li>item2</li>\n" +
                "    <li>item3</li>\n" +
                "</ul>\n" +
                "<p>asdjfalsdkjfa</p>\n" +
                "<p>sldfja&ouml;dlsfkjas</p>\n" +
                "<p>asdkfjasdlfa</p>\n" +
                "<h3><strong>THIS IS HEADING 2:</strong></h3>\n" +
                "<ul>\n" +
                "    <li><strong>item 4</strong></li>\n" +
                "    <li><strong>i</strong></li>\n" +
                "</ul>" ;
        new HTMLDialog("Help & Instructions",
                HELP_INSTRUCTION,
                false);

        }
}




