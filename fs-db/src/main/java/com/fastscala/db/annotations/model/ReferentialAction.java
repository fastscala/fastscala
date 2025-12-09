package com.fastscala.db.annotations.model;

public enum ReferentialAction {
  NO_ACTION("NO ACTION"),
  RESTRICT("RESTRICT"),
  CASCADE("CASCADE"),
  SET_NULL("SET NULL"),
  SET_DEFAULT("SET DEFAULT");

  public final String Sql;

  ReferentialAction(String sql) {
    this.Sql = sql;
  }
}
