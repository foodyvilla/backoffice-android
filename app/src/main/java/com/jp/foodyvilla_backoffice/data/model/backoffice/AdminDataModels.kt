package com.jp.foodyvilla_backoffice.data.model.backoffice

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

sealed interface AdminModel

@Serializable
data class DashboardData(
    val orders: List<Order> = emptyList(),
    val orderItems: List<OrderItem> = emptyList(),
    val products: List<ProductCatalog> = emptyList(),
    val users: List<User> = emptyList()
) : AdminModel

@Serializable
data class Outlet(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val name: String = "",
    val address: String? = null,
    val city: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    @SerialName("radius_km") val radiusKm: Double = 5.0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("opens_at") val opensAt: String? = null,
    @SerialName("closes_at") val closesAt: String? = null,
    @SerialName("fcm_tokens") val fcmTokens: List<String>? = null,
    @SerialName("banner_url") val bannerUrl: String? = null,
    @SerialName("razor_pay_key") val razorPayKey: String? = "rzp_test_ShBw7mlCM6gT6y"
) : AdminModel

@Serializable
data class Order(
    val id: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val instruction: String? = null,
    val phone: String? = null,
    @SerialName("delivery_lat") val deliveryLat: Double? = null,
    @SerialName("delivery_long") val deliveryLong: Double? = null,
    val address: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    val status: String = "pending",
    @SerialName("outlet_id") val outletId: Long? = null,
    @SerialName("customer_id") val customerId: Long? = null,
    @SerialName("order_type") val orderType: String? = null,
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("accepted_by") val acceptedBy: Long? = null,
    @SerialName("grand_total") val grandTotal: Long? = null,
    
    // Joined fields
    @SerialName("outlets") val outlet: Outlet? = null,
    @SerialName("users") val user: User? = null,
    @SerialName("employee") val acceptor: Employee? = null,
    @SerialName("payments") val payments: List<Payment>? = null
) : AdminModel

@Serializable
data class ProductCatalog(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val name: String = "",
    val description: String? = null,
    val review: JsonElement? = null,
    val category: String? = null,
    @SerialName("prep_time") val prepTime: String? = null,
    @SerialName("nutritional_info") val nutritionalInfo: JsonElement? = null,
    @SerialName("is_veg") val isVeg: Boolean = true,
    @SerialName("is_vegan") val isVegan: Boolean = false,
    @SerialName("is_bestseller") val isBestseller: Boolean = false,
    @SerialName("category_id") val categoryId: Long? = null
) : AdminModel

@Serializable
data class User(
    val id: Long? = null, // Users typically have bigint IDs in this schema
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("fcm_token") val fcmToken: String? = null,
    val address: String? = null,
    val lat: Double? = null,
    val long: Double? = null,
    @SerialName("auth_user_id") val authUserId: String? = null,
    @SerialName("is_verified") val isVerified: Boolean = false
) : AdminModel

@Serializable
data class Cart(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("customer_id") val customerId: Long? = null,
    @SerialName("outlet_id") val outletId: Long? = null,
    @SerialName("menu_item_id") val menuItemId: Long? = null,
    val qty: Long = 1,
    
    // Joined fields
    @SerialName("users") val user: User? = null,
    @SerialName("outlets") val outlet: Outlet? = null,
    @SerialName("outlet_menu_items") val outletMenuItem: OutletMenuItem? = null,
    
    // Display virtual fields
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("customer_phone") val customerPhone: String? = null,
    @SerialName("customer_fcm") val customerFcm: String? = null,
    @SerialName("outlet_name") val outletName: String? = null,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("product_price") val productPrice: Double? = null,
    @SerialName("product_image") val productImage: List<String>? = null
) : AdminModel

@Serializable
data class Banner(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("outlet_id") val outletId: Long? = null,
    val title: String? = null,
    @SerialName("img_url") val imgUrl: String? = null,
    @SerialName("display_order") val displayOrder: Int = 0
) : AdminModel

@Serializable
data class Offer(
    val id: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("outlet_id") val outletId: Long? = null,
    val title: String? = null,
    val description: String? = null,
    @SerialName("img_url") val imgUrl: String? = null,
    @SerialName("linked_url") val linkedUrl: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null
) : AdminModel

@Serializable
data class Review(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("customer_id") val customerId: Long? = null,
    @SerialName("review_type") val reviewType: String = "order",
    @SerialName("order_id") val orderId: String? = null,
    @SerialName("menu_item_id") val menuItemId: Long? = null,
    @SerialName("outlet_id") val outletId: Long? = null,
    val rating: Long = 5,
    val title: String? = null,
    val description: String? = null,
    @SerialName("img_url") val imgUrl: JsonElement? = null,
    
    // Joined fields
    @SerialName("users") val user: User? = null,
    @SerialName("outlet_menu_items") val outletMenuItem: OutletMenuItem? = null,
    @SerialName("outlets") val outlet: Outlet? = null
) : AdminModel

@Serializable
data class Employee(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("outlet_id") val outletId: Long? = null,
    val name: String = "",
    val address: String? = null,
    val contact: String? = null,
    @SerialName("aadhar_no") val aadharNo: String? = null,
    @SerialName("emergency_contact") val emergencyContact: String? = null,
    val salary: Long? = null,
    @SerialName("profile_img") val profileImg: String? = null,
    @SerialName("joining_date") val joiningDate: String? = null,
    @SerialName("punch_lat") val punchLat: Double? = null,
    @SerialName("punch_lng") val punchLng: Double? = null,
    val role: String? = null,
    @SerialName("password_hash") val passwordHash: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("auth_user_id") val authUserId: String? = null,
    @SerialName("fcm_token") val fcmToken: String? = null
) : AdminModel

@Serializable
data class Attendance(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("emp_id") val empId: Long? = null,
    val status: String? = null,
    @SerialName("in_time") val inTime: String? = null,
    @SerialName("out_time") val outTime: String? = null,
    @SerialName("in_lat") val inLat: Double? = null,
    @SerialName("in_lng") val inLng: Double? = null,
    @SerialName("out_lat") val outLat: Double? = null,
    @SerialName("out_lng") val outLng: Double? = null,
    
    // Joined fields
    @SerialName("employee") val employee: Employee? = null,
    
    // Virtual fields
    @SerialName("employee_name") val employeeName: String? = null,
    @SerialName("employee_role") val employeeRole: String? = null,
    @SerialName("employee_contact") val employeeContact: String? = null
) : AdminModel

@Serializable
data class OrderItem(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("order_id") val orderId: String? = null,
    @SerialName("menu_item_id") val menuItemId: Long? = null,
    val qty: Long = 1,
    @SerialName("price_per_item") val pricePerItem: Double = 0.0,
    @SerialName("total_price") val totalPrice: Double = 0.0,
    @SerialName("total_discount") val totalDiscount: Float = 0f,
    
    // Joined fields
    @SerialName("orders") val order: Order? = null,
    @SerialName("outlet_menu_items") val outletMenuItem: OutletMenuItem? = null,
    
    // Virtual fields
    @SerialName("order_label") val orderLabel: String? = null,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("product_category") val productCategory: String? = null,
    val image: List<String>? = null
) : AdminModel

@Serializable
data class OutletMenuItem(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("outlet_id") val outletId: Long? = null,
    @SerialName("product_id") val productId: Long? = null,
    val image: List<String>? = null,
    val price: Double = 0.0,
    val discount: Long = 0,
    @SerialName("is_available") val isAvailable: Boolean = true,
    @SerialName("is_out_of_stock") val isOutOfStock: Boolean = false,
    val rating: Float = 0f,
    @SerialName("reviews_count") val reviewsCount: Long = 0,
    @SerialName("handling_charges") val handlingCharges: Double? = null,
    @SerialName("delivery_charges") val deliveryCharges: Double? = null,
    @SerialName("is_free_delivery") val isFreeDelivery: Boolean? = null,
    
    // Joined fields
    @SerialName("product_catalog") val productCatalog: ProductCatalog? = null,
    @SerialName("outlets") val outlet: Outlet? = null,
    
    // Virtual fields
    @SerialName("product_name") val productName: String? = null,
    @SerialName("product_category") val productCategory: String? = null,
    @SerialName("product_description") val productDescription: String? = null,
    @SerialName("product_is_veg") val productIsVeg: Boolean? = null,
    @SerialName("product_prep_time") val productPrepTime: String? = null
) : AdminModel

@Serializable
data class Payment(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("order_id") val orderId: String? = null,
    @SerialName("customer_id") val customerId: Long? = null,
    @SerialName("razorpay_order_id") val razorpayOrderId: String? = null,
    @SerialName("razorpay_payment_id") val razorpayPaymentId: String? = null,
    @SerialName("razorpay_signature") val razorpaySignature: String? = null,
    val amount: Long = 0,
    @SerialName("amount_due") val amountDue: Long? = null,
    @SerialName("amount_refunded") val amountRefunded: Long = 0,
    val currency: String = "INR",
    @SerialName("payment_status") val paymentStatus: String = "created",
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("razorpay_response") val razorpayResponse: JsonElement? = null,
    @SerialName("refund_id") val refundId: String? = null,
    @SerialName("refund_reason") val refundReason: String? = null,
    @SerialName("refunded_at") val refundedAt: String? = null,
    @SerialName("error_code") val errorCode: String? = null,
    @SerialName("error_description") val errorDescription: String? = null,
    
    // Virtual fields
    @SerialName("order_customer") val orderCustomer: String? = null,
    @SerialName("order_status") val orderStatus: String? = null
) : AdminModel

@Serializable
data class AuthOtp(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val phone: String = "",
    val otp: String = "",
    @SerialName("expires_at") val expiresAt: String? = null
) : AdminModel

@Serializable
data class Category(
    val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val name: String = "",
    val emoji: String = "",
    @SerialName("is_active") val isActive: Boolean = true
) : AdminModel
