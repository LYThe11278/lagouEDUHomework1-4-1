package com.lagouedu.zkhomework.utils;

import com.alibaba.fastjson.JSONObject;
import com.lagouedu.zkhomework.model.JDBCConfig;
import com.lagouedu.zkhomework.model.ZkStrSerializer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ZkTest1 {
    private static HikariDataSource hikariDataSource;

    private static ZkClient zkClient;

    public static  void initZk() throws InterruptedException {
        zkClient = new ZkClient("ambari1:2181");
        zkClient.setZkSerializer(new ZkStrSerializer());
        //获取数据库连接信息
        JDBCConfig jdbcConfig = getJDBCConfig();
        //创建数据库连接池并进行测试
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcConfig.getUrl());
        config.setUsername(jdbcConfig.getUsername());
        config.setPassword(jdbcConfig.getPassword());
        config.setDriverClassName(jdbcConfig.getDriver());
        hikariDataSource = new HikariDataSource(config);
        Connection connection = null;
        try {
            connection = hikariDataSource.getConnection();
            PreparedStatement pst = connection.prepareStatement( "SELECT id, name FROM data;" );

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                System.out.println("id : " + rs.getString(1) + " , name : " + rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ZkTest1.updateZk();
    }

    public  static void updateZk() throws InterruptedException {

        zkClient = new ZkClient("ambari1:2181");

        zkClient.setZkSerializer(new ZkStrSerializer());

        zkClient.subscribeDataChanges("/mysql-jdbc", new IZkDataListener() {

            public void handleDataChange(String path, Object data) throws InterruptedException {

                System.out.println(path + " data is changed, new data " + data);

                //关闭连接
                hikariDataSource.close();
                //重新获取jdbc配置连接并测试
                updatedata();

            }

            public void handleDataDeleted(String path) throws InterruptedException {

                System.out.println(path + " is deleted!!");

                hikariDataSource.close();

            }
        });
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 1. 从 zookeeper 中获取配置信息
     * 2. 更新 hikari 配置并测试
     */
    public static void updatedata() throws InterruptedException {
        System.out.println("---------------进入方法updatedata---------------");
        JDBCConfig myConfig = getJDBCConfig();
        updateJdbcConfig(myConfig);
    }

    /**
     * 更新配置信息并进行连接测试
     * @param myConfig
     * @throws InterruptedException
     */
    private static void updateJdbcConfig(JDBCConfig myConfig) throws InterruptedException {
        System.out.println("---------------进入方法updatejdbcconfig-------------------");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(myConfig.getUrl());
        config.setUsername(myConfig.getUsername());
        config.setPassword(myConfig.getPassword());
        config.setDriverClassName(myConfig.getDriver());
        hikariDataSource = new HikariDataSource(config);
        Connection connection = null;
        try {
            connection = hikariDataSource.getConnection();
            PreparedStatement pst = connection.prepareStatement( "SELECT id, name FROM data;" );

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                System.out.println("id : " + rs.getString(1) + " , name : " + rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static JDBCConfig getJDBCConfig() {

        Object data = zkClient.readData("/mysql-jdbc");
        JDBCConfig jdbcConfig = JSONObject.parseObject(data.toString(), JDBCConfig.class);
        return jdbcConfig;
    }

}
