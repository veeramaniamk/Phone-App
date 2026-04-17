package com.veera.core.telephony.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class CallRepository_Factory implements Factory<CallRepository> {
  @Override
  public CallRepository get() {
    return newInstance();
  }

  public static CallRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CallRepository newInstance() {
    return new CallRepository();
  }

  private static final class InstanceHolder {
    static final CallRepository_Factory INSTANCE = new CallRepository_Factory();
  }
}
