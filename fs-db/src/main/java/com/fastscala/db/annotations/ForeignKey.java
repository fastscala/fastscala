package com.fastscala.db.annotations;

import com.fastscala.db.annotations.model.ReferentialAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({
  ElementType.FIELD,
  ElementType.METHOD,
  ElementType.PARAMETER,
  ElementType.LOCAL_VARIABLE
})
public @interface ForeignKey {

  public String refTbl();

  public String refCol();

  public ReferentialAction onDelete() default ReferentialAction.NO_ACTION;

  public ReferentialAction onUpdate() default ReferentialAction.NO_ACTION;
}
