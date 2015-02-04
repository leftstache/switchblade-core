package com.leftstache.switchblade.core;

import javax.lang.model.element.*;
import java.lang.annotation.*;

/**
 * @author Joel Johnson
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface Component {
}
