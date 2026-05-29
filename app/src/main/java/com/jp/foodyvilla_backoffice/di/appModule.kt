package com.jp.foodyvilla_backoffice.di

import com.jp.foodyvilla_backoffice.core.network.SupabaseLoggingInterceptor
import com.jp.foodyvilla_backoffice.data.domain.repository.OrderWorkflowRepository
import com.jp.foodyvilla_backoffice.data.domain.usecase.AcceptOrderUseCase
import com.jp.foodyvilla_backoffice.data.domain.usecase.MoveOrderStatusUseCase
import com.jp.foodyvilla_backoffice.data.domain.usecase.ObserveIncomingOrdersUseCase
import com.jp.foodyvilla_backoffice.data.domain.usecase.RejectOrderUseCase
import com.jp.foodyvilla_backoffice.data.repo.AdminRepository
import com.jp.foodyvilla_backoffice.data.repo.AuthRepo
import com.jp.foodyvilla_backoffice.data.repo.CartRepository
import com.jp.foodyvilla_backoffice.data.repo.LocationRepository
import com.jp.foodyvilla_backoffice.data.repo.OfferRepo
import com.jp.foodyvilla_backoffice.data.repo.ProductRepo
import com.jp.foodyvilla_backoffice.data.repo.ReviewRepository
import com.jp.foodyvilla_backoffice.data.repo.SupabaseAuthRepository
import com.jp.foodyvilla_backoffice.data.repo.TaskCategoryRepository
import com.jp.foodyvilla_backoffice.data.repo.UserRepository
import com.jp.foodyvilla_backoffice.data.repo.backoffice.BackOfficeCustomerRepository
import com.jp.foodyvilla_backoffice.data.repo.backoffice.BackOfficeFinanceRepository
import com.jp.foodyvilla_backoffice.data.repo.backoffice.BackOfficeOrderRepository
import com.jp.foodyvilla_backoffice.data.repo.backoffice.BackOfficeOutletRepository
import com.jp.foodyvilla_backoffice.data.repo.backoffice.BackOfficeProductRepository
import com.jp.foodyvilla_backoffice.data.repo.backoffice.BackOfficeReviewRepository
import com.jp.foodyvilla_backoffice.data.repo.backoffice.MarketingBackOfficeRepository
import com.jp.foodyvilla_backoffice.data.repo.backoffice.TeamBackOfficeRepository
import com.jp.foodyvilla_backoffice.data.repository.SupabaseOrderWorkflowRepository
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.MarketingRepository
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.menu.MarketingViewModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.NewOrdersManagementRepository
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.OutletManagementRepository
import com.jp.foodyvilla_backoffice.data.new_backoffice.repo.ProductCatalogRepository
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.OutletManagementViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.ProductCatalogViewModel
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.viewModels.UnifiedOrderControlViewModel

import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.AdminViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.DashboardViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.customers.CustomerViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.employees.TeamViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.orders.OrderViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.outlets.OutletViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.payments.FinanceViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.products.ProductViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.backoffice.reviews.ReviewViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.login.LoginViewModel
import com.jp.foodyvilla_backoffice.presentation.screens.task_category.TaskCategoryViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
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
    single { BackOfficeOrderRepository(get(), get()) }
    single { BackOfficeProductRepository(get(), get()) }
    single { MarketingBackOfficeRepository(get()) }
    single { TeamBackOfficeRepository(get(), get()) }
    single { BackOfficeCustomerRepository(get(), get()) }
    single { BackOfficeReviewRepository(get(), get()) }
    single { BackOfficeFinanceRepository(get(), get()) }
    single { BackOfficeOutletRepository(get(), get()) }
    single { ProductRepo(get()) }
    single{ ReviewRepository(get()) }
    single{ AuthRepo(get(), androidContext()) }
    single<AuthRepository> { SupabaseAuthRepository(get(), androidContext(), supabaseUrl, supabaseKey) }
    single { UserRepository(get()) }
    single{ CartRepository(get()) }
    single { TaskCategoryRepository(get()) }
    single<OrderWorkflowRepository> { SupabaseOrderWorkflowRepository(get()) }
    single { ObserveIncomingOrdersUseCase(get()) }
    single { AcceptOrderUseCase(get()) }
    single { MoveOrderStatusUseCase(get()) }
    single { RejectOrderUseCase(get()) }
    single{ LocationRepository(androidContext()) }

    viewModel {
        AdminViewModel(get())
    }

    viewModel {
        DashboardViewModel(get())
    }

    viewModel {
        OrderViewModel(get())
    }

    viewModel {
        ProductViewModel(get())
    }

    viewModel {
        com.jp.foodyvilla_backoffice.presentation.screens.backoffice.offers.MarketingViewModel(get())
    }

    viewModel {
        TeamViewModel(get())
    }

    viewModel {
        CustomerViewModel(get())
    }

    viewModel {
        ReviewViewModel(get())
    }

    viewModel {
        FinanceViewModel(get())
    }

    viewModel {
        OutletViewModel(get())
    }







    viewModel {
        TaskCategoryViewModel(get())
    }

    viewModel{
        LoginViewModel(get(), get(), get(), get())
    }




    singleOf(::NewOrdersManagementRepository)
    viewModelOf(::UnifiedOrderControlViewModel)


    // 3. Jetpack Architecture ViewModels
    single { ProductCatalogRepository(supabase = get()) }
    viewModel {
        ProductCatalogViewModel(
           get()
        )
    }
    singleOf(::MarketingRepository)

    // 2. Lifecycle-Aware Jetpack Scoped Architecture ViewModel
    viewModelOf(::MarketingViewModel)
    singleOf(::OutletManagementRepository)

    // 2. Lifecycle-Aware Jetpack Scoped Architecture ViewModel
    viewModelOf(::OutletManagementViewModel)
}
