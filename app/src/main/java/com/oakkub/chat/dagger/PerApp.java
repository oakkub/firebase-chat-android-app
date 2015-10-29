package com.oakkub.chat.dagger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by OaKKuB on 10/22/2015.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface PerApp {
}
