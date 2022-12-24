package com.infinity.sr2.toolkit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.infinity.sr2.toolkit.databinding.ActivityMainBinding;

import java.io.File;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    public static EditXmlUtil editXmlUtil = new EditXmlUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_craft_resize,
                R.id.nav_craft_collisions,
                R.id.nav_craft_Drag,
                R.id.nav_craft_resistance,
                R.id.nav_craft_appearance,
                R.id.nav_craft_other,
                R.id.nav_craft_god,
                R.id.nav_about
        )
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    protected void onStart() {
        // 请求存储权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1000);
        }

        /*
         * 多语言支持
         * */
        // 获取资源文件夹、获取设置对象、获取屏幕参数
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        //获取当前系统语言
        String locale_language = Locale.getDefault().getLanguage();

        if (locale_language.equals("zh")) {
            config.locale = Locale.CHINA;
            resources.updateConfiguration(config, dm);
        }

        super.onStart();
    }

    @Override
    protected void onResume() {

        Button outPutButton = findViewById(R.id.output_button);

        outPutButton.setOnClickListener(click -> {

            //集结所有项目的修改统一导出至xml
            Log.d("pkgPath", "正在导出Craft文件");
            String timeText = getResources().getString(R.string.result_time);
            String secondText = getResources().getString(R.string.second);

            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle(getResources().getString(R.string.output_file));
            //创建一个子线程
            new Thread(() -> {
                //关闭对话框
                Double resultTime = editXmlUtil.saveFile();
                dialog.dismiss();
                Snackbar.make(findViewById(R.id.fragment_view), timeText + resultTime + secondText, 5000).show();
            }).start();
            dialog.show();

            outPutButton.setVisibility(View.INVISIBLE);
        });

        super.onResume();
    }

    public void inputFileButton(View view) {
        // 导入存档文件
        File packagePath = getExternalFilesDir("");
        EditText fileName = findViewById(R.id.file_input);

        String filePath = packagePath + "/" + fileName.getText();
        Log.d("pkgPath", filePath);

        Log.d("pkgPath", "正在读取文件");
        // 开始读取文件
        if (new File(filePath + ".xml").exists()) {

            editXmlUtil.inputFile(filePath);
            if (editXmlUtil.getFileStatus() != null) {
                Snackbar.make(findViewById(R.id.input_craft), R.string.file_exists_true, 5000).show();
                Log.d("pkgPath", "文件读取成功");
            } else {
                System.out.println(editXmlUtil.getFileStatus());
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.warning))
                        .setMessage("文件导入失败"
                        ).create();
                dialog.show();
            }
        } else {
            Log.d("pkgPath", "文件不存在");
            Snackbar.make(findViewById(R.id.input_craft), R.string.file_exists_false, 8000).show();
        }

        Log.d("pkgPath", "软件包路径：" + filePath + ".xml");
        Log.d("pkgPath", String.valueOf(editXmlUtil.getFileStatus()));
    }

    /*
        存档缩放与质量修改接收文本框参数检测后传入
        craftResize 方法中
        true => 直径   false => 三维
    */
    public void craftResizeApply(View view) {

        if (editXmlUtil.getFileStatus() != null) {
            Button outPutButton = findViewById(R.id.output_button);
            EditText resizeNumber = findViewById(R.id.resize_number);
            EditText massNumber = findViewById(R.id.mass_number);
            RadioButton radioButton = findViewById(R.id.toWidth);
            EditXmlUtil editXmlUtil = new EditXmlUtil();

            String resize = String.valueOf(resizeNumber.getText());
            String mass = String.valueOf(massNumber.getText());

            if (!resize.equals("") && !resize.contains(" ")) {
                System.out.println("单选按钮状态：" + radioButton.isChecked());
                editXmlUtil.craftResize(radioButton.isChecked(), resize);
                outPutButton.setVisibility(View.VISIBLE);
            } else if (resize.contains(" ")) {
                alertSpaceWarning();
            }

            if (!mass.equals("") && !mass.contains(" ")) {
                editXmlUtil.editAttribute("massScale", mass);
                outPutButton.setVisibility(View.VISIBLE);
            } else if (mass.contains(" ")) {
                alertSpaceWarning();
            }
            // 还原默认零件质量
            if (mass.equals("default")) {
                editXmlUtil.removeAttribute("massScale");
            }

            if (resize.equals("") && mass.equals("")) {
                alertDoubleNullWarning();
            }

        } else {
            alertNotFileWarning();
        }

    }


    //警告弹窗
    private void alertSpaceWarning() {  //  警告不能输入空格弹窗
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.error2)
                ).create();
        dialog.show();
    }

    private void alertDoubleNullWarning() { // 警告不能所有输入框都为空
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.error3)
                ).create();
        dialog.show();
    }

    private void alertNotFileWarning() { // 警告应先导入文件
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.error1)).create();
        dialog.show();
    }


    //存档碰撞监听
    public void collisionsApply(View view) {

        if (editXmlUtil.getFileStatus() != null) {
            Spinner collisionsList = findViewById(R.id.collisions_model);
            Spinner collisionsResponseList = findViewById(R.id.collisions_response_list);
            Button outputButton = findViewById(R.id.output_button);

            // 获取下拉列表选中的ID值
            int collisionsItemValue = (int) collisionsList.getSelectedItemId();
            int collisionsResponseItemValue = (int) collisionsResponseList.getSelectedItemId();

            // 碰撞箱模式逻辑
            switch (collisionsItemValue) {
                case 0:
                    editXmlUtil.editAttribute("partCollisionHandling", "Default");
                    break;
                case 1:
                    editXmlUtil.editAttribute("partCollisionHandling", "Always");
                    break;
                case 2:
                    editXmlUtil.editAttribute("partCollisionHandling", "Never");
                    break;
            }

            // 碰撞箱反应模式逻辑
            switch (collisionsResponseItemValue) {
                case 0:
                    editXmlUtil.editAttribute("partCollisionResponse", "Default");
                    break;
                case 1:
                    editXmlUtil.editAttribute("partCollisionResponse", "None");
                    break;
                case 2:
                    editXmlUtil.editAttribute("partCollisionResponse", "DisconnectOnly");
                    break;
            }

            outputButton.setVisibility(View.VISIBLE);
        } else {
            alertNotFileWarning();
        }

    }


    //存档阻力监听
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public void dragApply(View view) {

        if (editXmlUtil.getFileStatus() != null) {
            EditText dragNumber = findViewById(R.id.craft_Drag_number);
            Switch dragToggle = findViewById(R.id.drag_model);
            Button outputButton = findViewById(R.id.output_button);

            String dragScaleNumber = String.valueOf(dragNumber.getText());
            // 阻力UI逻辑
            if (!dragScaleNumber.equals("") && !dragScaleNumber.contains(" ")) {
                editXmlUtil.editAttribute("dragScale", dragScaleNumber);
            } else if (dragScaleNumber.contains(" ")) {
                alertSpaceWarning();
            }
            // 还原默认阻力
            if (dragScaleNumber.equals("default")) {
                editXmlUtil.removeAttribute("dragScale");
            }

            // 阻力开关
            editXmlUtil.editAttribute("includeInDrag", String.valueOf(dragToggle.isChecked()));

            outputButton.setVisibility(View.VISIBLE);
        } else {
            alertNotFileWarning();
        }

    }


    //存档抗性监听
    public void resistanceApply(View view) {

        if (editXmlUtil.getFileStatus() != null) {
            Button outputButton = findViewById(R.id.output_button);
            EditText damageNumber = findViewById(R.id.damage_number);
            EditText heatResistNumber = findViewById(R.id.heat_resist_number);

            String damageValue = String.valueOf(damageNumber.getText());
            String heatResistValue = String.valueOf(heatResistNumber.getText());
            // 文本框逻辑处理
            if (!damageValue.equals("") && !damageValue.contains(" ")) {
                editXmlUtil.editAttribute("maxDamage", damageValue);
            } else if (damageValue.contains(" ")) {
                alertSpaceWarning();
            }

            if (!heatResistValue.equals("") && !heatResistValue.contains(" ")) {
                editXmlUtil.editAttribute("heatShield", heatResistValue);
                editXmlUtil.removeAttribute("heatShieldScale");
            } else if (heatResistValue.contains(" ")) {
                alertSpaceWarning();
            }
            // 还原默认值
            if (damageValue.equals("default")) {
                editXmlUtil.removeAttribute("maxDamage");
            }
            if (heatResistValue.equals("default")) {
                editXmlUtil.removeAttribute("heatShield");
            }

            if (damageValue.equals("") && heatResistValue.equals("")) {
                alertDoubleNullWarning();
            }

            outputButton.setVisibility(View.VISIBLE);
        } else {
            alertNotFileWarning();
        }

    }


    //存档外观属性监听
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public void appearanceApply(View view) {

        if (editXmlUtil.getFileStatus() != null) {
            Button outputButton = findViewById(R.id.output_button);
            Switch partBitReserve = findViewById(R.id.part_bit_reserve_switch);
            Switch partTransparent = findViewById(R.id.part_transparent_switch);
            Switch partShadow = findViewById(R.id.part_shadow_switch);
            Switch fuelTanksTexture = findViewById(R.id.fuel_tanks_texture_model);

            // 零件碎片化保留
            editXmlUtil.editAttribute("preventDebris", String.valueOf(partBitReserve.isChecked()));
            // 零件透明化
            editXmlUtil.editAttribute("supportsTransparency", String.valueOf(partTransparent.isChecked()));
            // 零件阴影
            editXmlUtil.editAttribute("castShadows", String.valueOf(partShadow.isChecked()));
            // 清除油箱纹理
            editXmlUtil.clearFuelTankTexture(fuelTanksTexture.isChecked());

            outputButton.setVisibility(View.VISIBLE);
        } else {
            alertNotFileWarning();
        }

    }


    //存档其他设置监听
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public void otherApply(View view) {

        if (editXmlUtil.getFileStatus() != null) {
            Button outputButton = findViewById(R.id.output_button);
            EditText colorBlockNumber = findViewById(R.id.color_block_number);
            EditText inertiaTensorNumber = findViewById(R.id.inertia_tensor_number);
            Switch fuelLineToggle = findViewById(R.id.fuel_line_switch);

            String colorBlockValue = String.valueOf(colorBlockNumber.getText());
            String inertiaTensorValue = String.valueOf(inertiaTensorNumber.getText());

            // 其他设置逻辑层
            if (!colorBlockValue.equals("") && !colorBlockValue.contains(" ")) {
                editXmlUtil.addColorBlock(colorBlockValue);
            } else if (colorBlockValue.contains(" ")) {
                alertSpaceWarning();
            }
            if (!inertiaTensorValue.equals("") && !inertiaTensorValue.contains(" ")) {
                editXmlUtil.editAttribute("inertiaTensorUserScale", inertiaTensorValue);
            } else if (inertiaTensorValue.contains(" ")) {
                alertSpaceWarning();
            }

            if (colorBlockValue.equals("") && inertiaTensorValue.equals("")) {
                alertDoubleNullWarning();
            }

            // 燃料管道开关
            if (fuelLineToggle.isChecked()) {
                editXmlUtil.editAttribute("fuelLine", String.valueOf(fuelLineToggle.isChecked()));
            } else {
                editXmlUtil.removeAttribute("fuelLine");
            }

            outputButton.setVisibility(View.VISIBLE);
        } else {
            alertNotFileWarning();
        }

    }

    /*
     * 上帝模式代码
     * */
    public void godModeApply(View view) {
        if (editXmlUtil.getFileStatus() != null) {
            Button outputButton = findViewById(R.id.output_button);
            // 一键无敌大礼包
            editXmlUtil.editAttribute("maxDamage", "3.4e+38");
            editXmlUtil.editAttribute("collisionDisconnectImpulse", "3.4e+38");
            editXmlUtil.editAttribute("collisionDisconnectVelocity", "3.4e+38");
            editXmlUtil.editAttribute("collisionExplodeImpulse", "3.4e+38");
            editXmlUtil.editAttribute("collisionExplodeVelocity", "3.4e+38");
            editXmlUtil.editAttribute("collisionPreventExternalDisconnections", "true");

            outputButton.setVisibility(View.VISIBLE);
        } else {
            alertNotFileWarning();
        }
    }


    //使用说明按钮
    public void loadCraftExplain(View view) {
        // 导入文件使用说明
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.load_craft_file_1))
                .setMessage(getResources().getString(R.string.load_craft_file_2)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.load_craft_file_3)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.load_craft_file_4)
                ).create();
        dialog.show();
    }

    public void craftResizeExplain(View view) {
        // 存档缩放使用说明
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.craft_resize_1))
                .setMessage(getResources().getString(R.string.craft_resize_2)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.craft_resize_3)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.craft_resize_4)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.craft_resize_5)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.craft_resize_6)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.craft_resize_7)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.craft_resize_8)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.craft_resize_9)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.craft_resize_10)
                ).create();
        dialog.show();
    }

    public void dragExplain(View view) {
        // 存档阻力使用说明
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.drag_1))
                .setMessage(getResources().getString(R.string.drag_2)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.drag_3)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.drag_4)
                ).create();
        dialog.show();
    }

    public void collisionsExplain(View view) {
        // 存档碰撞说明
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.collisions_1))
                .setMessage(getResources().getString(R.string.collisions_2)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.collisions_3)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.collisions_4)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.collisions_5)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.collisions_6)
                ).create();
        dialog.show();
    }

    public void resistanceExplain(View view) {
        // 存档抗性说明
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.resistance_1))
                .setMessage(getResources().getString(R.string.resistance_2)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.resistance_3)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.resistance_4)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.resistance_5)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.resistance_6)
                ).create();
        dialog.show();
    }

    public void appearanceExplain(View view) {
        // 存档外观说明
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.appearance_1))
                .setMessage(getResources().getString(R.string.appearance_2)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.appearance_3)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.appearance_4)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.appearance_5)
                ).create();
        dialog.show();
    }

    public void otherExplain(View view) {
        // 存档其他说明
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.other_1))
                .setMessage(getResources().getString(R.string.other_2)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.other_3)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.other_4)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.other_5)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.other_6)
                ).create();
        dialog.show();
    }

    public void godModeExplain(View view) {
        // 存档其他说明
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.god_mode))
                .setMessage(getResources().getString(R.string.god_mode_1)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.god_mode_2)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.god_mode_3)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.god_mode_4)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.god_mode_5)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.god_mode_6)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.god_mode_7)
                ).create();
        dialog.show();
    }


    // 跳转网页
    public void linkWebSite(String webLink) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(webLink);
        intent.setData(content_url);
        startActivity(intent);
    }

    // 跳转作者官网
    public void linkDevWebSite(View view) {
        linkWebSite("https://www.simplerockets.com/u/JasonCaesar007");
    }

    // 跳转APP官网
    public void linkAppWebSite(View view) {
        linkWebSite("https://www.simplerockets.com/u/JasonCaesar007");
    }

    // 跳转官方QQ群
    public void linkQqGroup(View view) {
        linkWebSite("https://jq.qq.com/?_wv=1027&k=F0s03zqH");
    }

    // 跳转Tg群组
    public void linkTelegramGroup(View view) {
        linkWebSite("https://t.me/+KfJBBjHhgZwzNzk1");
    }

    // 跳转Tg群组
    public void linkDom4jWebsite(View view) {
        linkWebSite("https://dom4j.github.io/");
    }

    // Dom4j开源许可证
    public void dom4jSourceLicense(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.dom4j_title))
                .setMessage(getResources().getString(R.string.dom4j_license_1)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.dom4j_license_2)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.dom4j_license_3)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.dom4j_license_4)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.dom4j_license_5)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.dom4j_license_6)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.dom4j_license_7)
                        + "\r\n" +
                        " "
                        + "\r\n" +
                        getResources().getString(R.string.dom4j_license_8)
                ).create();
        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.clear_craft){
            if (editXmlUtil.setReplaceCraft()) {
                Snackbar.make(findViewById(R.id.fragment_view), R.string.clear_text, 5000).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //同意申请权限
                getExternalFilesDir("");
                Toast.makeText(this, getResources().getString(R.string.rw_permissions_true), Toast.LENGTH_SHORT).show();
            } else {
                // 用户拒绝申请权限
                Toast.makeText(this, getResources().getString(R.string.rw_permissions_false), Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}