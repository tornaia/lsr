package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.exception.IllegalMavenStateException;

public interface Action {

    void execute() throws IllegalMavenStateException;
}
