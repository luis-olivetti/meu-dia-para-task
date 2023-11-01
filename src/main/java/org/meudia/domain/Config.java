package org.meudia.domain;

import lombok.Data;

import java.util.List;

@Data
public class Config {
    private String projectCode;
    private String username;
    private int teamCode;
    private String defaultCode;
    private String tip;
    private List<MapTask> tasksMap;
}
