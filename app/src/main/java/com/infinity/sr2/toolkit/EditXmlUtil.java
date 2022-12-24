package com.infinity.sr2.toolkit;

import android.util.Log;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EditXmlUtil {

    public static Document read;

    public String fileName;

    private Double resizeNumber;

    private Document oldRead;

    public void inputFile(String fileName) {

        this.fileName = fileName;

        SAXReader saxReader = new SAXReader();

        try {
            read = saxReader.read(new File(fileName + ".xml"));
            this.oldRead = saxReader.read(new File(fileName + ".xml"));
            System.out.println("旧的数据：" + this.oldRead);
        } catch (DocumentException e) {
            Log.d("pkgPath", "Dom解析失败");
        }
    }

    public boolean setReplaceCraft() {
        if (read != null && oldRead != null) {
            read = this.oldRead;
            return true;
        } else {
            return false;
        }
    }

    public Document getFileStatus() {
        return read;
    }

    /*
     * 增加或修改零件Config属性的方法
     * */
    public void editAttribute(String partAttribute, String partAttributeValue) {
        Element rootElement = read.getRootElement();
        List<Element> parts = rootElement.element("Assembly").element("Parts").elements("Part");

        parts.forEach(ret -> {
            Element config = ret.element("Config");
            config.addAttribute(partAttribute, partAttributeValue);
        });
    }

    /*
     * 删除Config属性值
     * */
    public void removeAttribute(String partAttribute) {
        Element rootElement = read.getRootElement();
        List<Element> parts = rootElement.element("Assembly").element("Parts").elements("Part");

        parts.forEach(ret -> {
            Element config = ret.element("Config");
            if (config.attribute(partAttribute) != null) { // 还原默认值
                config.remove(config.attribute(partAttribute));
            }
        });
    }

    public void clearFuelTankTexture(Boolean model) {
        Element rootElement = read.getRootElement();
        List<Element> parts = rootElement.element("Assembly").element("Parts").elements("Part");

        if (!model) {
            parts.forEach(ret -> {
                if (ret.attribute("partType").getText().equals("Fuselage1")) {
                    ret.addAttribute("texture", "Default");
                }
            });
        }
    }

    /*
     * 添加颜色格子
     * */
    public void addColorBlock(String colorBlockNumber) {
        Element rootEmt = read.getRootElement();
        // 添加颜色格子功能
        if (!colorBlockNumber.equals("")) {
            // 获取DesignerSettings子节点
            List<Element> designerSettings = rootEmt.elements("DesignerSettings");

            designerSettings.forEach(ret -> {
                // 获取颜色格子所在的Theme字节点
                Element theme = ret.element("Theme");
                for (int i = 0; i < Integer.parseInt(colorBlockNumber); i++) {
                    // 循环添加颜色格子的Material子节点以及属性
                    Element material = theme.addElement("Material");
                    material.addAttribute("color", "FFFFFF");
                    material.addAttribute("m", "0");
                    material.addAttribute("s", "0");
                }
            });
            // 获取Themes子节点
            List<Element> themes = rootEmt.elements("Themes");
            themes.forEach(ret -> {
                // 获取颜色格子所在的Theme字节点
                Element theme = ret.element("Theme");
                for (int i = 0; i < Integer.parseInt(colorBlockNumber); i++) {
                    // 循环添加颜色格子的Material子节点以及属性
                    Element material = theme.addElement("Material");
                    material.addAttribute("color", "FFFFFF");
                    material.addAttribute("m", "0");
                    material.addAttribute("s", "0");
                }
            });
        }
    }

    /*
     * 存档整体缩放算法
     *
     * 直接调用 craftResize方法即可
     * */
    public void craftResize(Boolean model, String resizeNumber) {
        this.resizeNumber = Double.parseDouble(resizeNumber);

        Element rootElement = read.getRootElement();
        List<Element> parts = rootElement.element("Assembly").element("Parts").elements("Part");

        parts.forEach(ret -> {

            // 修改零件坐标系，拉开距离
            String[] position = ret.attribute("position").getText().split(",");
            ret.addAttribute("position", Double.parseDouble(position[0]) * this.resizeNumber
                    + "," +
                    Double.parseDouble(position[1]) * this.resizeNumber
                    + "," +
                    Double.parseDouble(position[2]) * this.resizeNumber);

            // 缩放起落架
            if (ret.attribute("partType").getText().equals("LandingGear1")) {
                Element landingGear = ret.element("LandingGear");

                if (landingGear.attribute("size") != null)
                    landingGear.addAttribute("size", editResizeAttribute(1, landingGear.attribute("size").getText()));
                else {
                    landingGear.addAttribute("size", String.valueOf(this.resizeNumber));
                }
            }
        });

        if (!model) {

            // 仅修改三维
            parts.forEach(ret -> {
                Element config = ret.element("Config");
                if (config.attribute("partScale") != null) {
                    String[] partScale = config.attribute("partScale").getText().split(",");
                    config.addAttribute("partScale", Double.parseDouble(partScale[0]) * this.resizeNumber
                            + "," +
                            Double.parseDouble(partScale[1]) * this.resizeNumber
                            + "," +
                            Double.parseDouble(partScale[2]) * this.resizeNumber);
                } else {
                    config.addAttribute("partScale", this.resizeNumber + "," + this.resizeNumber + "," + this.resizeNumber);
                }

            });

        } else {

            // 修改各部件的尺寸属性
            parts.forEach(ret -> {
                // 指令舱
                if (ret.attribute("partType").getText().equals("CommandPod4") ||
                        ret.attribute("partType").getText().equals("CommandPod2") ||
                        ret.attribute("partType").getText().equals("CommandPod5") ||
                        ret.attribute("partType").getText().equals("CommandPod6") ||
                        ret.attribute("partType").getText().equals("CommandPod3") ||
                        ret.attribute("partType").getText().equals("Cockpit1") ||
                        ret.attribute("partType").getText().equals("EvaChair1") ||
                        ret.attribute("partType").getText().equals("CommandChip1") ||
                        ret.attribute("partType").getText().equals("RotatingNoseCone1") ||
                        ret.attribute("partType").getText().equals("CockpitCanopy1") ||
                        ret.attribute("partType").getText().equals("Eva") ||
                        ret.attribute("partType").getText().equals("Eva-Tourist")
                ) {
                    Element config = ret.element("Config");

                    if (config.attribute("partScale") != null)
                        config.addAttribute("partScale", editResizeAttribute(3, config.attribute("partScale").getText()));
                    else {
                        config.addAttribute("partScale", this.resizeNumber + "," + this.resizeNumber + "," + this.resizeNumber);
                    }
                }

                // 圆筒、结构体、整流罩、进气道、级间分离器、陀螺仪、隔热底、鼻锥、货舱、等圆筒类零件
                if (ret.attribute("partType").getText().equals("Fuselage1") ||
                        ret.attribute("partType").getText().equals("Strut1") ||
                        ret.attribute("partType").getText().equals("FairingBase1") ||
                        ret.attribute("partType").getText().equals("Fairing1") ||
                        ret.attribute("partType").getText().equals("FairingNoseCone1") ||
                        ret.attribute("partType").getText().equals("Inlet1") ||
                        ret.attribute("partType").getText().equals("Detacher1") ||
                        ret.attribute("partType").getText().equals("Gyroscope1") ||
                        ret.attribute("partType").getText().equals("HeatShield1") ||
                        ret.attribute("partType").getText().equals("NoseCone1") ||
                        ret.attribute("partType").getText().equals("CargoBay1") ||
                        ret.attribute("partType").getText().equals("CrewCompartment")
                ) {
                    Element fuselage = ret.element("Fuselage");

                    fuselage.addAttribute("bottomScale", editResizeAttribute(2, fuselage.attribute("bottomScale").getText()));
                    fuselage.addAttribute("offset", editResizeAttribute(3, fuselage.attribute("offset").getText()));
                    fuselage.addAttribute("topScale", editResizeAttribute(2, fuselage.attribute("topScale").getText()));
                }

                // 火箭发动机
                if (ret.attribute("partType").getText().equals("RocketEngine1")) {
                    Element rocketEngine = ret.element("RocketEngine");

                    if (rocketEngine.attribute("size") != null)
                        rocketEngine.addAttribute("size", editResizeAttribute(1, rocketEngine.attribute("size").getText()));
                    else {
                        rocketEngine.addAttribute("size", String.valueOf(this.resizeNumber));
                    }
                }

                // 电推
                if (ret.attribute("partType").getText().equals("IonEngine1")) {
                    Element engine = ret.element("Engine");

                    if (engine.attribute("scale") != null)
                        engine.addAttribute("scale", editResizeAttribute(1, engine.attribute("scale").getText()));
                    else {
                        engine.addAttribute("scale", String.valueOf(this.resizeNumber));
                    }
                }

                // 航空发动机
                if (ret.attribute("partType").getText().equals("JetEngine1")) {
                    Element jetEngine = ret.element("JetEngine");

                    jetEngine.addAttribute("size", editResizeAttribute(1, jetEngine.attribute("size").getText()));
                }

                // 姿态调节发动机(RCS)
                if (ret.attribute("partType").getText().equals("RCSNozzle1") || ret.attribute("partType").getText().equals("RCSNozzle2")) {
                    Element reactionControlNozzle = ret.element("ReactionControlNozzle");

                    if (reactionControlNozzle.attribute("scale") != null)
                        reactionControlNozzle.addAttribute("scale", editResizeAttribute(1, reactionControlNozzle.attribute("scale").getText()));
                    else {
                        reactionControlNozzle.addAttribute("scale", String.valueOf(this.resizeNumber));
                    }
                }

                // 转轴、铰链
                if (ret.attribute("partType").getText().equals("Rotator1") || ret.attribute("partType").getText().equals("HingeRotator1")) {
                    Element jointRotator = ret.element("JointRotator");

                    if (jointRotator.attribute("scale") != null)
                        jointRotator.addAttribute("scale", editResizeAttribute(1, jointRotator.attribute("scale").getText()));
                    else {
                        jointRotator.addAttribute("scale", String.valueOf(this.resizeNumber));
                    }

                }

                //降落伞
                if (ret.attribute("partType").getText().equals("Parachute1")) {
                    Element parachute = ret.element("Parachute");

                    if (parachute.attribute("baseSize") != null)
                        parachute.addAttribute("baseSize", editResizeAttribute(1, parachute.attribute("baseSize").getText()));
                    else {
                        parachute.addAttribute("baseSize", String.valueOf(1.25 * this.resizeNumber));
                    }
                }
                // 灯
                if (ret.attribute("partType").getText().equals("Light1")) {
                    Element lightPart = ret.element("LightPart");

                    if (lightPart.attribute("scale") != null)
                        lightPart.addAttribute("scale", editResizeAttribute(1, lightPart.attribute("scale").getText()));
                    else {
                        lightPart.addAttribute("scale", String.valueOf(this.resizeNumber));
                    }
                }
                if (ret.attribute("partType").getText().equals("BeaconLight1")) {
                    Element beaconLight = ret.element("BeaconLight");

                    if (beaconLight.attribute("scale") != null)
                        beaconLight.addAttribute("scale", editResizeAttribute(1, beaconLight.attribute("scale").getText()));
                    else {
                        beaconLight.addAttribute("scale", String.valueOf(this.resizeNumber));
                    }
                }
                // 燃料适配器、开关、假人、对接口、方块
                if (ret.attribute("partType").getText().equals("TestPilot") ||
                        ret.attribute("partType").getText().equals("DockingPort1") ||
                        ret.attribute("partType").getText().equals("ToggleSwitch1") ||
                        ret.attribute("partType").getText().equals("FuelAdapter1") ||
                        ret.attribute("partType").getText().equals("Block1")
                ) {
                    Element config = ret.element("Config");

                    if (config.attribute("partScale") != null)
                        config.addAttribute("partScale", editResizeAttribute(3, config.attribute("partScale").getText()));
                    else {
                        config.addAttribute("partScale", this.resizeNumber + "," + this.resizeNumber + "," + this.resizeNumber);
                    }
                }
                // 机翼、结构板
                if (ret.attribute("partType").getText().equals("Wing1") ||
                        ret.attribute("partType").getText().equals("Fin1") ||
                        ret.attribute("partType").getText().equals("StructuralPanel1")
                ) {
                    Element wing = ret.element("Wing");

                    wing.addAttribute("rootLeadingOffset", editResizeAttribute(1, wing.attribute("rootLeadingOffset").getText()));
                    wing.addAttribute("rootTrailingOffset", editResizeAttribute(1, wing.attribute("rootTrailingOffset").getText()));
                    wing.addAttribute("tipLeadingOffset", editResizeAttribute(1, wing.attribute("tipLeadingOffset").getText()));
                    wing.addAttribute("tipTrailingOffset", editResizeAttribute(1, wing.attribute("tipTrailingOffset").getText()));
                    wing.addAttribute("tipPosition", editResizeAttribute(3, wing.attribute("tipPosition").getText()));
                }
                // 着陆腿
                if (ret.attribute("partType").getText().equals("LandingLeg2") ||
                        ret.attribute("partType").getText().equals("LandingLeg3") ||
                        ret.attribute("partType").getText().equals("LandingLeg4")
                ) {
                    Element landingLeg = ret.element("LandingLeg");

                    if (landingLeg.attribute("scale") != null)
                        landingLeg.addAttribute("scale", editResizeAttribute(1, landingLeg.attribute("scale").getText()));
                    else {
                        landingLeg.addAttribute("scale", String.valueOf(this.resizeNumber));
                    }
                }
                // 杂项
                if (ret.attribute("partType").getText().equals("DetacherSide1")) { // 侧边分离器
                    Element detacher = ret.element("Detacher");

                    if (detacher.attribute("scale") != null)
                        detacher.addAttribute("scale", editResizeAttribute(1, detacher.attribute("scale").getText()));
                    else {
                        detacher.addAttribute("scale", String.valueOf(this.resizeNumber));
                    }
                }
                if (ret.attribute("partType").getText().equals("Piston1")) { // 活塞
                    Element piston = ret.element("Piston");

                    if (piston.attribute("scale") != null)
                        piston.addAttribute("scale", editResizeAttribute(1, piston.attribute("scale").getText()));
                    else {
                        piston.addAttribute("scale", String.valueOf(this.resizeNumber));
                    }
                }
                if (ret.attribute("partType").getText().equals("Shock1")) { // 弹簧
                    Element suspension = ret.element("Suspension");

                    if (suspension.attribute("size") != null)
                        suspension.addAttribute("size", editResizeAttribute(1, suspension.attribute("size").getText()));
                    else {
                        suspension.addAttribute("size", String.valueOf(this.resizeNumber));
                    }
                }
                if (ret.attribute("partType").getText().equals("Label1")) { // 文本框(Label)
                    Element label = ret.element("Label");

                    if (label.attribute("fontSize") != null) {
                        label.addAttribute("fontSize", editResizeAttribute(1, label.attribute("fontSize").getText()));
                    } else {
                        label.addAttribute("fontSize", String.valueOf(this.resizeNumber));
                    }
                }
                if (ret.attribute("partType").getText().equals("Gauge1")) { // 仪表盘
                    Element gauge = ret.element("Gauge");

                    if (gauge.attribute("scale") != null) {
                        gauge.addAttribute("scale", editResizeAttribute(1, gauge.attribute("scale").getText()));
                    } else {
                        gauge.addAttribute("scale", String.valueOf(this.resizeNumber));
                    }
                }
                if (ret.attribute("partType").getText().equals("Mfd1")) { // 显示屏(MFD)
                    Element mfd = ret.element("Mfd");

                    if (mfd.attribute("width") != null) {
                        mfd.addAttribute("width", editResizeAttribute(1, mfd.attribute("width").getText()));
                    } else {
                        mfd.addAttribute("width", String.valueOf(0.5 * this.resizeNumber));
                    }

                    if (mfd.attribute("height") != null) {
                        mfd.addAttribute("height", editResizeAttribute(1, mfd.attribute("height").getText()));
                    } else {
                        mfd.addAttribute("height", String.valueOf(0.5 * this.resizeNumber));
                    }

                }
                if (ret.attribute("partType").getText().equals("SolarPanel1")) { // 太阳能电池板
                    Element solarPanel = ret.element("SolarPanel");

                    if (solarPanel.attribute("width") != null) {
                        solarPanel.addAttribute("width", editResizeAttribute(1, solarPanel.attribute("width").getText()));
                    } else {
                        solarPanel.addAttribute("width", String.valueOf(1 * this.resizeNumber));
                    }

                    if (solarPanel.attribute("length") != null) {
                        solarPanel.addAttribute("length", editResizeAttribute(1, solarPanel.attribute("length").getText()));
                    } else {
                        solarPanel.addAttribute("length", String.valueOf(1.3 * this.resizeNumber));
                    }

                }
                if (ret.attribute("partType").getText().equals("SolarPanelArray")) { // 太阳能电池阵列
                    Element solarPanelArray = ret.element("SolarPanelArray");

                    if (solarPanelArray.attribute("scale") != null) {
                        solarPanelArray.addAttribute("scale", editResizeAttribute(1, solarPanelArray.attribute("scale").getText()));
                    } else {
                        solarPanelArray.addAttribute("scale", String.valueOf(this.resizeNumber));
                    }
                }
                if (ret.attribute("partType").getText().equals("ElectricMotor1")) { // 电机
                    Element electricMotor = ret.element("ElectricMotor");

                    if (electricMotor.attribute("scale") != null) {
                        electricMotor.addAttribute("scale", editResizeAttribute(1, electricMotor.attribute("scale").getText()));
                    } else {
                        electricMotor.addAttribute("scale", String.valueOf(this.resizeNumber));
                    }
                }
                if (ret.attribute("partType").getText().equals("RoverWheel1")) { // 车轮
                    Element electricMotor = ret.element("ResizableWheel");

                    if (electricMotor.attribute("size") != null) {
                        electricMotor.addAttribute("size", editResizeAttribute(1, electricMotor.attribute("size").getText()));
                    } else {
                        electricMotor.addAttribute("size", String.valueOf(this.resizeNumber));
                    }
                }
                if (ret.attribute("partType").getText().equals("Generator1")
                        || ret.attribute("partType").getText().equals("Generator2")) { // 燃料电池
                    Element electricMotor = ret.element("Generator");

                    if (electricMotor.attribute("scale") != null) {
                        electricMotor.addAttribute("scale", editResizeAttribute(1, electricMotor.attribute("scale").getText()));
                    } else {
                        electricMotor.addAttribute("scale", String.valueOf(this.resizeNumber));
                    }
                }

            });
        }

    }

    // 存档缩放算法的剩余部分
    private String editResizeAttribute(int model, String attribute) {
        // 乘以指定的倍数
        switch (model) {
            case 1:
                return String.valueOf(Double.parseDouble(attribute) * this.resizeNumber);

            case 2:
                String[] attributeList2 = attribute.split(",");
                return Double.parseDouble(attributeList2[0]) * this.resizeNumber
                        + "," +
                        Double.parseDouble(attributeList2[1]) * this.resizeNumber;

            case 3:
                String[] attributeList3 = attribute.split(",");
                return Double.parseDouble(attributeList3[0]) * this.resizeNumber
                        + "," +
                        Double.parseDouble(attributeList3[1]) * this.resizeNumber
                        + "," +
                        Double.parseDouble(attributeList3[2]) * this.resizeNumber;
        }
        return null;
    }

    // 创建输出流保存文件
    public Double saveFile() {
        long start = System.currentTimeMillis();
        OutputFormat outputFormat = OutputFormat.createPrettyPrint();
        outputFormat.setEncoding(StandardCharsets.UTF_8.name());
        try {
            FileOutputStream fos = new FileOutputStream(this.fileName + "-Edit.xml");
            XMLWriter xmlWriter = new XMLWriter(fos, outputFormat);
            xmlWriter.write(read);
            xmlWriter.close();
            long endTime = System.currentTimeMillis();
            return (endTime - start) / 1000.0;
        } catch (IOException e) {
            Log.d("saveFile", "文件保存失败");
        }
        return null;
    }
}