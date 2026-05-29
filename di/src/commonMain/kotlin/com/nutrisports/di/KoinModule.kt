package com.nutrisports.di

import com.nutrisports.auth.AuthViewModel
import com.nutrisports.data.CustomerRepositoryImpl
import com.nutrisports.data.domain.CustomerRepository
import com.nutrisports.home.HomeGraphViewModel
import com.nutrisports.profile.ProfileViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val sharedModule = module {
    single<CustomerRepository>{ CustomerRepositoryImpl() }
    viewModelOf(::AuthViewModel)
    viewModelOf(::HomeGraphViewModel)
    viewModelOf(::ProfileViewModel)
}
fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null
) {
    startKoin {
        config?.invoke(this)
        modules(sharedModule)
    }
}