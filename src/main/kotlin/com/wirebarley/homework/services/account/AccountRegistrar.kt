package com.wirebarley.homework.services.account

import com.wirebarley.homework.jpa.entities.account.Accounts
import com.wirebarley.homework.jpa.entities.account.AccountsRepository
import com.wirebarley.homework.services.account.command.CreateNewAccountCommand
import com.wirebarley.homework.services.common.exception.AlreadyExistException
import com.wirebarley.homework.services.common.exception.ExistDataType
import com.wirebarley.homework.util.lock.distributed.RedisDistributedLock
import org.springframework.stereotype.Service

@Service
class AccountRegistrar(private val accountsRepository: AccountsRepository) {

  /**
   * 계좌를 신규 개설합니다.
   *
   * @param command 계좌 신규개설 명령서
   */
  @RedisDistributedLock(key = "#create-new-account")
  fun register(command: CreateNewAccountCommand) {
    val phoneNumberExists = accountsRepository.existsByOwnerPhoneNumber(command.phoneNumber)

    if (phoneNumberExists) {
      throw AlreadyExistException(ExistDataType.PHONE_NUMBER, "이미 사용중인 전화번호입니다")
    }

    val emailExists = accountsRepository.existsByOwnerEmail(command.email)

    if (emailExists) {
      throw AlreadyExistException(ExistDataType.EMAIL, "이미 사용중인 이메일 주소입니다.")
    }

    val newAccount = Accounts.createNewAccount(
      command.name,
      command.phoneNumber,
      command.email,
      command.requester
    )

    accountsRepository.save(newAccount)
  }
}
