package com.helmuth.shell.command;

import org.springframework.shell.standard.ShellMethod;

public class UserCommands {
    @ShellMethod(value = "This command ends up in the 'User Commands' group")
    public void foo() {}

    @ShellMethod(value = "This command ends up in the 'Other Commands' group",
            group = "Other Commands")
    public void bar() {}
}
