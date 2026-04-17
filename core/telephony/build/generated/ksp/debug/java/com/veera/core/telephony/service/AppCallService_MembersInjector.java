package com.veera.core.telephony.service;

import com.veera.core.telephony.repository.CallRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class AppCallService_MembersInjector implements MembersInjector<AppCallService> {
  private final Provider<CallRepository> callRepositoryProvider;

  public AppCallService_MembersInjector(Provider<CallRepository> callRepositoryProvider) {
    this.callRepositoryProvider = callRepositoryProvider;
  }

  public static MembersInjector<AppCallService> create(
      Provider<CallRepository> callRepositoryProvider) {
    return new AppCallService_MembersInjector(callRepositoryProvider);
  }

  public static MembersInjector<AppCallService> create(
      javax.inject.Provider<CallRepository> callRepositoryProvider) {
    return new AppCallService_MembersInjector(Providers.asDaggerProvider(callRepositoryProvider));
  }

  @Override
  public void injectMembers(AppCallService instance) {
    injectCallRepository(instance, callRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.veera.core.telephony.service.AppCallService.callRepository")
  public static void injectCallRepository(AppCallService instance, CallRepository callRepository) {
    instance.callRepository = callRepository;
  }
}
