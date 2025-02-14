package net.hearnsoft.tcm.beans;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfile {
    private String name;
    private String avatar_name;
    private Date last_login;
    private int[] roles;
}
