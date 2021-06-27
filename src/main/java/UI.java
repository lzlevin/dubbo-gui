import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.net.telnet.TelnetClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <br>
 *
 * @author levin
 * @version 1.1
 */
public class UI {
    private JTextArea response;
    private JButton ok;
    private JTextField host;
    private JTextField port;
    private JPanel hello;
    private JTextField clazz;
    private JTextArea request;
    private JButton 美化请求参数Button;
    private JButton chorme转JsonButton;

    public UI() {

        ok.addMouseListener(new MouseListener() {

            /**
             * Invoked when the mouse button has been clicked (pressed
             * and released) on a component.
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                if (StrUtil.isBlank(host.getText())) {
                    JOptionPane.showMessageDialog(hello, "主机不能为空");
                    return;
                }
                if (StrUtil.isBlank(port.getText())) {
                    JOptionPane.showMessageDialog(hello, "端口不能为空");
                    return;
                }
                if (!NumberUtil.isInteger(port.getText())) {
                    JOptionPane.showMessageDialog(hello, "端口错误");
                    return;
                }
                if (StrUtil.isBlank(clazz.getText())) {
                    JOptionPane.showMessageDialog(hello, "类路径不能为空");
                    return;
                }
                if (StrUtil.isBlank(request.getText())) {
                    JOptionPane.showMessageDialog(hello, "请求参数不能为空");
                    return;
                }
                if (request.getText().trim().startsWith("{")) {
                    doRequset(host.getText(), port.getText(), clazz.getText(), request.getText().trim());
                } else if (request.getText().trim().startsWith("'")) {
                    doRequset(host.getText(), port.getText(), clazz.getText(), request.getText().trim());
                } else {
                    doRequset(host.getText(), port.getText(), clazz.getText(), "'" + request.getText().trim() + "'");
                }
            }

            /**
             * Invoked when a mouse button has been pressed on a component.
             *
             * @param e
             */
            @Override
            public void mousePressed(MouseEvent e) {

            }

            /**
             * Invoked when a mouse button has been released on a component.
             *
             * @param e
             */
            @Override
            public void mouseReleased(MouseEvent e) {

            }

            /**
             * Invoked when the mouse enters a component.
             *
             * @param e
             */
            @Override
            public void mouseEntered(MouseEvent e) {

            }

            /**
             * Invoked when the mouse exits a component.
             *
             * @param e
             */
            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        美化请求参数Button.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    JSONArray objects = JSON.parseArray("[" + request.getText() + "]");
                    String json = JSON.toJSONString(objects, SerializerFeature.PrettyFormat);
                    int start = json.indexOf("[");
                    int end = json.lastIndexOf("]");
                    String substring = json.substring(start + 1, end).trim();
                    if (substring.startsWith("{")) {
                        request.setText("\t" + substring);
                    } else {
                        request.setText(substring);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(hello, "美化失败");
                }
            }
        });
        chorme转JsonButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    String chormeText = request.getText();
                    String[] split = chormeText.split("\n");
                    JSONObject jsonObject = new JSONObject();
                    for (String line : split) {
                        String replace = line.replaceAll("\\s+", "");
                        String[] split2 = replace.split(":");
                        String substring = replace.substring(split2[0].length() + 1);
                        if (substring.startsWith("[") || substring.startsWith("{")) {
                            jsonObject.put(split2[0], JSON.parse(substring));
                        } else {
                            jsonObject.put(split2[0], substring);
                        }
                    }
                    String json = JSON.toJSONString(jsonObject, SerializerFeature.PrettyFormat);
                    request.setText(json);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(hello, "美化失败");
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("dubbo直连测试");
        frame.setContentPane(new UI().hello);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(800, 600);
    }

    Pattern pattern = Pattern.compile("result:([\\s\\S]*)elapsed");

    public void doRequset(String host, String port, String service, String args) {
        try {
            TelnetClient telnetClient = new TelnetClient("vt200");
            telnetClient.setCharset(CharsetUtil.charset(CharsetUtil.UTF_8));
            telnetClient.setDefaultTimeout(5000);
            telnetClient.connect(host, NumberUtil.parseInt(port));
            String content = "invoke " + service.replace("#", ".") + "(" + args + ")" + "\r\n";
            IoUtil.write(telnetClient.getOutputStream(), CharsetUtil.UTF_8,
                    false, content);
            InputStream inputStream = telnetClient.getInputStream();
            ArrayList<Integer> bytes = new ArrayList<>(2000);
            int size;
            String suffix = "";
            String defaultSuffix = "dubbo>";
            int[] dubboBytes = new int[]{'d', 'u', 'b', 'b', 'o', '>'};
            StringBuilder sb = new StringBuilder();
            int tempSize = bytes.size();
            while (true) {
                bytes.add(inputStream.read());
                if ((tempSize = bytes.size()) > 6 && (bytes.get(tempSize - 1) == dubboBytes[5]
                        && bytes.get(tempSize - 2) == dubboBytes[4] && bytes.get(tempSize - 3) == dubboBytes[3]
                        &&
                        bytes.get(tempSize - 4) == dubboBytes[2]
                        && bytes.get(tempSize - 5) == dubboBytes[1] && bytes.get(tempSize - 6) == dubboBytes[0]

                )) {
                    break;
                }

            }
            byte[] finalBytes = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); i++) {
                finalBytes[i] = (byte) bytes.get(i).intValue();
            }
            String responseText = new String(finalBytes, Charset.forName(CharsetUtil.UTF_8));
            if (responseText.length() < dubboBytes.length) {
                response.setText(responseText);
            } else {
                Matcher matcher = pattern.matcher(responseText);
                if (matcher.find()) {
                    String group = matcher.group(1);
                    try {
                        Object parse = JSON.parse(group);
                        response.setText(JSON.toJSONString(parse, SerializerFeature.PrettyFormat));
                    } catch (Exception ex) {
                        response.setText(group);
                    }
                } else {
                    response.setText(responseText.replace(defaultSuffix, ""));
                }
            }
            telnetClient.disconnect();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(hello, "连接失败");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(hello, "程序异常");
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        hello = new JPanel();
        hello.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        hello.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        ok = new JButton();
        ok.setText("确定");
        panel1.add(ok, new com.intellij.uiDesigner.core.GridConstraints(0, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("主机");
        panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        host = new JTextField();
        host.setText("127.0.0.1");
        host.setToolTipText("如127.0.0.1");
        panel1.add(host, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("端口");
        panel1.add(label2, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        port = new JTextField();
        port.setText("20881");
        port.setToolTipText("如127.0.0.1");
        panel1.add(port, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        hello.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("方法全路径");
        panel2.add(label3, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        clazz = new JTextField();
        clazz.setText("com.vin.admin.service.UserService#getUser");
        clazz.setToolTipText("Idea可以右键方法选择Copy Refrence");
        panel2.add(clazz, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        美化请求参数Button = new JButton();
        美化请求参数Button.setText("美化请求参数");
        panel2.add(美化请求参数Button, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chorme转JsonButton = new JButton();
        chorme转JsonButton.setText("Chorme转Json");
        panel2.add(chorme转JsonButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        hello.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        response = new JTextArea();
        response.setToolTipText("响应参数");
        scrollPane1.setViewportView(response);
        final JScrollPane scrollPane2 = new JScrollPane();
        hello.add(scrollPane2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 200), null, null, 0, false));
        request = new JTextArea();
        request.setText("请求参数支持普通类型，如123456，支持json类型的pojo，如{\"name\":\"张三\"}，多参数请用,分割，如{\"name\":\"张三\"},{\"name\":\"\"里斯}");
        request.setToolTipText("请求参数支持普通类型，如123456，支持json类型的pojo，如{\"name\":\"张三\"}，多参数请用,分割，如{\"name\":\"张三\"},{\"name\":\"\"里斯}");
        scrollPane2.setViewportView(request);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return hello;
    }

}
