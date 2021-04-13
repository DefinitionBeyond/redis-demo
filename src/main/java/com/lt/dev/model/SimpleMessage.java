package com.lt.dev.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleMessage<T extends Serializable> {
    private String publisher;
    private T content;
    private Date createTime;
}
