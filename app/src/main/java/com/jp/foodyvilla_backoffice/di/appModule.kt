package com.jp.foodyvilla_backoffice.di

import com.jp.foodyvilla_backoffice.data.repo.AuthRepo
import com.jp.foodyvilla_backoffice.data.repo.AdminRepository
import com.jp.foodyvilla_backoffice.data.repo.CartRepository
import com.jp.foodyvilla_backoffice.data.repo.LocationRepository
import com.jp.foodyvilla_backoffice.data.repo.OfferRepo
import com.jp.foodyvilla_backoffice.data.repo.OrderRepository
import com.jp.foodyvilla_backoffice.data.repo.ProductRepo
import com.jp.foodyvilla_backoffice.data.repo.ReviewRepository
import com.jp.foodyvilla_backoffice.data.repo.UserRepository
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.AdminViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.detail.DetailViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.login.LoginViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.menu.MenuViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.offers.OffersViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.reviews.ReviewsViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module{

    single {
        createSupabaseClient(
            supabaseUrl = "https://mzeajzfhjovwyuotiywx.supabase.co",
            supabaseKey = "sb_publishable_C0Dz4fVE-_YjQIHLHqMbQQ_EWWuskzq"
        ) {
            install(Auth){
                autoLoadFromStorage  = true
                alwaysAutoRefresh = true

            }
            install(Postgrest)
            install(Storage)
            install(Functions)
            install(Realtime)


            httpEngine = OkHttp.create()
        }
    }


    single { OfferRepo(get()) }
    single { AdminRepository(get()) }
    single { ProductRepo(get()) }
    single{ ReviewRepository(get()) }
    single{ AuthRepo(get(), androidContext()) }
    single { UserRepository(get()) }
    single{ CartRepository(get()) }
    single{ OrderRepository(get()) }
    single{ LocationRepository(androidContext()) }
    viewModel {
        HomeViewModel(get(), get(), get(),get(), get())
    }
    viewModel {
        AdminViewModel(get())
    }
    viewModel{
        OffersViewModel(get())
    }

    viewModel{
        DetailViewModel(get(),get())
    }

    viewModel{
        MenuViewModel(get())
    }

    viewModel{
        ReviewsViewModel(get())
    }

    viewModel{
        LoginViewModel(get(), get(), get())
    }

}
