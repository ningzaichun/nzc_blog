package com.nzc.demo.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nzc.demo.handler.MyDateTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * 
 * </p>
 *
 * @author Ning Zaichun
 * @since 2022-08-08
 */
@Data
@TableName(value = "handler_test",autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
public class HandlerTest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    /**
     * 存时间戳
     */
    @TableField(typeHandler = MyDateTypeHandler.class)
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date;
}
