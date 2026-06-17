package com.nutrisports.di

import com.nutrisport.manage_product.ManageProductViewModel
import com.nutrisports.auth.AuthViewModel
import com.nutrisports.data.AdminRepositoryImpl
import com.nutrisports.data.CustomerRepositoryImpl
import com.nutrisports.data.domain.AdminRepository
import com.nutrisports.data.domain.CustomerRepository
import com.nutrisports.home.HomeGraphViewModel
import com.nutrisports.profile.ProfileViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val sharedModule = module {
    single<CustomerRepository>{ CustomerRepositoryImpl() }
    single<AdminRepository>{ AdminRepositoryImpl() }
    viewModelOf(::AuthViewModel)
    viewModelOf(::HomeGraphViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::ManageProductViewModel)
}

expect val targetModule: Module
fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null
) {
    startKoin {
        config?.invoke(this)
        modules(sharedModule, targetModule)
    }
}