package com.ziroom.godeye.constant;

/**
 * <p>Description: 日志储存</p>
 *
 * @author: by qlb
 * @date: 2017/6/7  18:01
 * @version: 1.0
 */
public enum StorageTypeEnum {

    MYSQL("mysql","mysql"),
    ES("es","elasticSearch"),
    LOG_LOCAL("log_local","日志本地"),
    LOG_PROXY("log_proxy","日志代理"),
    MQ("mq","消息中心");

    private String code;
    private String name;

    private StorageTypeEnum(String code,String name){
        this.code = code;
        this.name = name;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
