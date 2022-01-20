 package com.gitegg.platform.base.service;

 /**
  * @author GitEgg
  */
 @FunctionalInterface
 public interface IAction<T> {

     /**
      * 执行回调
      * @param param
      */
     void run(T param);
 }

