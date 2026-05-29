package com.nutrisports.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.Country
import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.domain.PhoneNumber
import com.nutrisport.shared.util.RequestState
import com.nutrisports.data.domain.CustomerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class ProfileScreenState(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val city: String = "",
    val postalCode: Int? = null,
    val address: String = "",
    val phoneNumber: PhoneNumber? = null,
    val country: Country = Country.Serbia
)

class ProfileViewModel(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    val customer = customerRepository.readCustomerFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RequestState.Idle
        )




    var screenReady: RequestState<Unit> by mutableStateOf(RequestState.Loading)
    var screenState by mutableStateOf(ProfileScreenState())
        private set

    val isFormValid: Boolean
        get() = with(screenState) {
            firstName.length in 3..50 && lastName.length in 3..50 &&
                    city.length in 3..50 && postalCode.toString().length in 3..8
                    && address.length in 3..50 && phoneNumber?.number?.length in 3..10
        }

    init {
        viewModelScope.launch {
            customer.collectLatest { data ->
                if (data.isSuccess()) {
                    val fetchCustomer = data.getSuccessData()
                    screenState = ProfileScreenState(
                        id = fetchCustomer.id,
                        firstName = fetchCustomer.firstName,
                        lastName = fetchCustomer.lastName,
                        email = fetchCustomer.email,
                        city = fetchCustomer.city ?: "",
                        postalCode = fetchCustomer.postalCode,
                        address = fetchCustomer.address ?: "",
                        phoneNumber = fetchCustomer.phoneNumber,
                        country = Country.entries.firstOrNull { it.dialCode == fetchCustomer.phoneNumber?.dialCode } ?: Country.Serbia
                    )
                    screenReady = RequestState.Success(Unit)
                } else if (data.isError()) {
                    screenReady = RequestState.Error(data.getErrorMessage())
                }
            }
        }
    }

    fun updateFirstName(firstName : String) {
        screenState = screenState.copy(
            firstName = firstName
        )
    }

    fun updateCountry(country: Country) {
        screenState = screenState.copy(
            country = country,
            phoneNumber = screenState.phoneNumber?.copy(
                dialCode = country.dialCode
            )
        )
    }

    fun updateLastName(lastName: String) {
        screenState = screenState.copy(
            lastName = lastName
        )
    }

    fun updateCity(city: String) {
        screenState = screenState.copy(
            city = city
        )
    }

    fun updatePostalCode(postalCode: Int?) {
        screenState = screenState.copy(
            postalCode = postalCode
        )
    }

    fun updateAddress(address: String) {
        screenState = screenState.copy(
            address = address
        )
    }

    fun updatePhoneNumber(value: String) {
        screenState = screenState.copy(
            phoneNumber = PhoneNumber(
                dialCode = screenState.country.dialCode,
                number = value
            )
        )
    }

    fun updateCustomerInfo(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            customerRepository.updateCustomer(
                customer = Customer(
                    id = screenState.id,
                    firstName = screenState.firstName,
                    lastName = screenState.lastName,
                    email = screenState.email,
                    city = screenState.city,
                    postalCode = screenState.postalCode,
                    address = screenState.address,
                    phoneNumber = screenState.phoneNumber
                ),
                onSuccess = onSuccess,
                onError = onError
            )
        }

    }


}