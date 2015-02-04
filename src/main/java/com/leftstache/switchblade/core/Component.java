package com.leftstache.switchblade.core;

import com.google.inject.*;

import javax.lang.model.element.*;
import java.lang.annotation.*;

/**
 * @author Joel Johnson
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@ScopeAnnotation
public @interface Component {
}
