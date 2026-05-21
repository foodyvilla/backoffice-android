package com.jp.foodyvilla_backoffice.di

import com.jp.foodyvilla_backoffice.core.network.SupabaseLoggingInterceptor
import com.jp.foodyvilla_backoffice.data.repo.AuthRepo
import com.jp.foodyvilla_backoffice.data.repo.AdminRepository
import com.jp.foodyvilla_backoffice.data.repo.CartRepository
import com.jp.foodyvilla_backoffice.data.repo.LocationRepository
import com.jp.foodyvilla_backoffice.data.repo.OfferRepo
import com.jp.foodyvilla_backoffice.data.repo.OrderRepository
import com.jp.foodyvilla_backoffice.data.repo.ProductRepo
import com.jp.foodyvilla_backoffice.data.repo.ReviewRepository
import com.jp.foodyvilla_backoffice.data.repo.SupabaseAuthRepository
import com.jp.foodyvilla_backoffice.data.repo.UserRepository
import com.jp.foodyvilla_backoffice.data.domain.repository.OrderWorkflowRepository
import com.jp.foodyvilla_backoffice.data.domain.usecase.AcceptOrderUseCase
import com.jp.foodyvilla_backoffice.data.domain.usecase.MoveOrderStatusUseCase
import com.jp.foodyvilla_backoffice.data.domain.usecase.ObserveIncomingOrdersUseCase
import com.jp.foodyvilla_backoffice.data.domain.usecase.RejectOrderUseCase
import com.jp.foodyvilla_backoffice.data.repository.SupabaseOrderWorkflowRepository
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
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

    val supabaseUrl = "https://qxqnwfcljizyscrqkntd.supabase.co"
    val supabaseKey = "sb_publishable_P2vCR3YTVxyHShA8Gbb0RQ_HxXAGqZ-"

    single {
        createSupabaseClient(
//            supabaseUrl = "https://mzeajzfhjovwyuotiywx.supabase.co",
//            supabaseKey = "sb_publishable_C0Dz4fVE-_YjQIHLHqMbQQ_EWWuskzq"


            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Auth){
                autoLoadFromStorage  = true
                alwaysAutoRefresh = true

            }
            install(Postgrest)
            install(Storage)
            install(Functions)
            install(Realtime)


            httpEngine = OkHttp.create {
                config {
                    addInterceptor(SupabaseLoggingInterceptor())
                }
            }
        }
    }


    single { OfferRepo(get()) }
    single { AdminRepository(get(), get(), get()) }
    single { ProductRepo(get()) }
    single{ ReviewRepository(get()) }
    single{ AuthRepo(get(), androidContext()) }
    single<AuthRepository> { SupabaseAuthRepository(get(), androidContext(), supabaseUrl, supabaseKey) }
    single { UserRepository(get()) }
    single{ CartRepository(get()) }
    single{ OrderRepository(get()) }
    single<OrderWorkflowRepository> { SupabaseOrderWorkflowRepository(get()) }
    single { ObserveIncomingOrdersUseCase(get()) }
    single { AcceptOrderUseCase(get()) }
    single { MoveOrderStatusUseCase(get()) }
    single { RejectOrderUseCase(get()) }
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
        LoginViewModel(get(), get(), get(), get())
    }

}
