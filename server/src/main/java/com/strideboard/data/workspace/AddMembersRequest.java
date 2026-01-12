package com.strideboard.data.workspace;

import java.util.List;

import lombok.Data;

@Data
public class AddMembersRequest {
    private List<String> emails;
}
