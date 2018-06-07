with V_CONFIG as
     (
         select nvl(
                (
                    select ATTRIBUTE1 
                      from TB_CM_CODE_VALUE
                     where COMPANY_CD     = :companyCd
                       and CLASS_CD       = 'SYSTEM_PROFILE'
                       and CLASS_CD_VALUE = 'CREATED_DT_YN'
                ), 'N') as CREATED_DT_YN
              , nvl(
                (
                    select ATTRIBUTE1 
                      from TB_CM_CODE_VALUE
                     where COMPANY_CD     = :companyCd
                       and CLASS_CD       = 'SYSTEM_PROFILE'
                       and CLASS_CD_VALUE = 'BIZ_UNIT_UNION_SETTLE_YN'
                ), 'N') as BIZ_UNION_YN
           from dual
     )
   , V_ACCOUNT as
     (
         select COMPANY_CD, CLASS_CD, ATTRIBUTE2
           from TB_CM_CODE_VALUE
          where ( COMPANY_CD, CLASS_CD, CLASS_CD_VALUE ) =
                (
                    select COMPANY_CD, ATTRIBUTE1, :setoffTp
                      from TB_CM_CODE_VALUE
                     where COMPANY_CD     = :companyCd
                       and CLASS_CD       = 'SETOFF_TP'
                       and CLASS_CD_VALUE = :accountTp
                )
           and  :rcvPayTp = '3'
     )
   , V_PROJECT_INFO as
     (
         select COMPANY_CD
              , PROJECT_CD
              , PROJECT_NM
           from VI_PM_PROJECT_INFO
          where COMPANY_CD   = :companyCd
            and LANGUAGE_CD  = :languageCd
         union
         select A.COMPANY_CD
              , A.CLASS_CD_VALUE
              , B.META_ITEM_DESC
           from TB_CM_CODE_VALUE    A
           ,    TB_CM_LANGUAGE_PACK B
          where 1=1
            and A.LANGUAGE_PACK_ID = B.MGMT_OBJECT_ID

            and A.COMPANY_CD       = :companyCd
            and A.CLASS_CD         = 'PMS_PROJECT_CD'
            and B.LANGUAGE_CD      = :languageCd
     )
select /* 정산대상 - 상계에사만 상대내역 조회 [ FmRefSettle_V02 ]*/
       A.COMPANY_CD                     "companyCd"         -- 회사코드
     , A.BIZ_UNIT_CD                    "bizUnitCd"         -- 사업장코드
     , A.RCP_PAY_NO                     "rcpPayNo"          -- 상계번호
     , A.CREATED_DT                     "createdDt"         -- 작성일자
     , A.PAYMENT_DT                     "paymentDt"         -- 결제예정일자
     , A.PAYMENT_METHOD_CD              "paymentMethodCd"   -- 결제구분
     , A.DEMAND_NO                      "demandNo"          -- 청구서번호
     , A.DEMAND_NO_SEQ                  "demandNoSeq"       -- 청구서번호순번
     , A.SLIP_NO                        "slipNo"            -- 발생전표번호
     , A.SLIP_LINE_NO                   "slipLineNo"        -- 발생전표행번호
     , A.ACCOUNT_CD                     "accountCd"         -- 계정코드
     , PKG_GLOBAL.GET_LANGUAGE_ACCOUNT_NM
       (
           A.COMPANY_CD, A.ACCOUNT_CD, :languageCd
       )                                "accountNm"         -- 계정명
     , B.DRCR_TP                        "drcrTp"            -- 차대구분
     , A.CUST_CD                        "custCd"            -- 거래처코드
     , C.CUST_NM                        "custNm"            -- 거래처명
     , A.TRADE_CURRENCY_CD              "tradeCurrencyCd"   -- 거래통화코드
     , A.TRADE_EXCHANGE_RATE            "tradeExchangeRate" -- 거래환율
     , nvl(A.TRADE_AMT       , 0)       "tradeAmt"          -- 거래금액
     , nvl(A.CALC_AMT        , 0)       "calcAmt"           -- 환산금액
     , nvl(A.SETTLE_TRADE_AMT, 0)       "settleTradeAmt"    -- 정산된금액(정산된금액 - 현재작업중인 [입금/지급/상계] 제외)
     , nvl(A.SETTLE_TRANS_AMT, 0)       "settleTransAmt"    -- 정산된환산금액(정산된금액 - 현재작업중인 [입금/지급/상계] 제외)
     , nvl(A.PAY_TRADE_AMT   , 0)       "payTradeAmt"       -- 정산할금액
     , nvl(A.PAY_TRANS_AMT   , 0)       "payTransAmt"       -- 환산정산할금액
     , nvl(A.REMAIN_TRADE_AMT, 0)       "remainTradeAmt"    -- 정산잔액
     , nvl(A.REMAIN_TRANS_AMT, 0)       "remainTransAmt"    -- 환산정산잔액
     , decode
       (
           sign(A.DAYS)
         , 1, A.DAYS
         , 0
       )                                "passedDay"         -- 경과일
     , A.REMARKS                        "remarks"           -- 특기사항(비고)
     , A.DEPT_CD                        "deptCd"            -- 귀속부서
     , D.DEPT_NM                        "deptNm"            -- 귀속부서명
     , A.CHARGE_EMP_NO                  "chargeEmpNo"       -- 담당자직번
     , E.EMP_NM                         "chargeEmpNm"       -- 담당자명
     , A.PURCHASE_DUTY_TP               "purchaseDutyTp"    -- 구매정면세구분
     , A.SALES_DUTY_TP                  "salesDutyTp"       -- 판매정면세구분
     , A.FA_NO                          "faNo"              -- 자산번호
     , A.FA_PROC_TP                     "faProcTp"          -- 자산처리구분
     , A.CREDIT_CARD_CD                 "creditCardCd"      -- 법인카드코드
     , F.CREDIT_CARD_NO                 "creditCardNo"      -- 번인카드번호
     , F.CARD_OWNER                     "cardUser"          -- 카드사용자직번
     , G.EMP_NM                         "cardUserNm"        -- 카드사용자명
     , H.INV_TP                         "invTp"             -- 재고구분
     , H.INVOICE_NO                     "invoiceNo"         -- 송장번호
     , A.PROJECT_CD                     "projectCd"         -- 프로젝트코드
     , P.PROJECT_NM                     "projectNm"         -- 프로젝트명
  from
   (
       select A.COMPANY_CD
            , A.BIZ_UNIT_CD
            , ''                         as RCP_PAY_NO
            , A.DEMAND_NO
            , A.DEMAND_NO_SEQ
            , A.CREATED_DT
            , A.SLIP_NO
            , A.SLIP_LINE_NO
            , A.ACCOUNT_CD
            , A.PAYMENT_METHOD_CD
            , A.PAYMENT_CONFIRM_DT       as PAYMENT_DT
            , (
                  to_date(nvl(:paymentDt, to_char(sysdate, 'YYYYMMDD')), 'YYYYMMDD')
                - to_date(A.PAYMENT_CONFIRM_DT, 'YYYYMMDD')
              ) as DAYS
            , A.CUST_CD
            , A.TRADE_CURRENCY_CD
            , A.EVE_EXCHANGE_RATE        as TRADE_EXCHANGE_RATE
            , nvl(A.TRADE_AMT       , 0) as TRADE_AMT
            , nvl(A.EVE_CALC_AMT    , 0) as CALC_AMT
            , nvl(A.SETTLE_TRADE_AMT, 0) as SETTLE_TRADE_AMT
            , nvl(A.SETTLE_TRANS_AMT, 0) as SETTLE_TRANS_AMT
            , 0                          as PAY_TRADE_AMT
            , 0                          as PAY_TRANS_AMT
            , (
                  nvl(A.TRADE_AMT       , 0)
                - nvl(A.SETTLE_TRADE_AMT, 0)
              )                          as REMAIN_TRADE_AMT
            , (
                  nvl(A.CALC_AMT       , 0)
                - nvl(A.SETTLE_TRANS_AMT, 0)
              )                          as REMAIN_TRANS_AMT
            , A.REMARKS
            , nvl
              (
                  A.DEPT_CD
                , (
                      select SA.DEPT_CD
                        from TB_CM_EMP SA
                       where SA.COMPANY_CD = A.COMPANY_CD
                         and SA.EMP_NO     = A.CHARGE_EMP_NO
                  )
              ) as DEPT_CD
            , A.CHARGE_EMP_NO
            , A.PURCHASE_DUTY_TP
            , A.SALES_DUTY_TP
            , A.FA_NO
            , A.FA_PROC_TP
            , A.CREDIT_CARD_CD
            , A.PROJECT_CD
            , A.UPPER_DEMAND_NO_SEQ
         from TB_FI_REVPAY_SETTLE_DIVIDE A
         ,    V_CONFIG                   V
        where 1 = 1
          and A.COMPANY_CD          = :companyCd
          and A.CUST_CD             = nvl(:custCd         , A.CUST_CD           )
          and A.TRADE_CURRENCY_CD   = nvl(:tradeCurrencyCd, A.TRADE_CURRENCY_CD )
          and A.PAYMENT_METHOD_CD   = nvl(:paymentMethodCd, A.PAYMENT_METHOD_CD )
          and A.ACCOUNT_CD         in (
                                          select ATTRIBUTE1
                                            from TB_CM_CODE_VALUE
                                           where COMPANY_CD  = :companyCd
                                             and CLASS_CD    = ( select CLASS_CD   from V_ACCOUNT )
                                             and ATTRIBUTE2 <> ( select ATTRIBUTE2 from V_ACCOUNT )
                                      )
          and (
                  (
                      abs(nvl(A.TRADE_AMT       ,0))
                    - abs(nvl(A.SETTLE_TRADE_AMT,0))
                  ) <> 0
               or (
                      abs(nvl(A.EVE_CALC_AMT    ,0))
                    - abs(nvl(A.SETTLE_TRANS_AMT,0))
                  ) <> 0
              )

          and (
                  nvl(A.TRADE_AMT       , 0)
                - nvl(A.DIVIDE_TRADE_AMT, 0)
              ) > 0

          and ( A.COMPANY_CD, A.SLIP_NO, A.SLIP_LINE_NO, A.DEMAND_NO, A.DEMAND_NO_SEQ ) not in
              (
                  select COMPANY_CD, SLIP_NO, SLIP_LINE_NO, DEMAND_NO, DEMAND_NO_SEQ
                    from TB_FI_REVPAY_SETTLE_HISTORY
                   where COMPANY_CD = :companyCd
                     and RCP_PAY_NO = :rcpPayNo
              )

          and case when V.BIZ_UNION_YN       = 'Y'
                        then 'Y'
                   when V.BIZ_UNION_YN       = 'N'
                    and A.BIZ_UNIT_CD        = :bizUnitCd
                        then 'Y'
              end = 'Y'
                        
          and case 
                   when V.CREATED_DT_YN      = 'Y'
                    and :startDt             is not null
                    and :endDt               is not null
                    and A.CREATED_DT         between :startDt
                                                 and :endDt
                        then 'Y'
                   when V.CREATED_DT_YN      = 'Y'
                    and :startDt             is null
                    and :endDt               is null
                    and A.PAYMENT_CONFIRM_DT <= nvl(:paymentDt, A.PAYMENT_CONFIRM_DT)
                        then 'Y'
                   when V.CREATED_DT_YN      = 'N'
                    and :startDt             is not null
                    and :endDt               is not null
                    and A.PAYMENT_CONFIRM_DT between :startDt
                                                 and :endDt
                        then 'Y'
                   when :startDt             is null
                    and :endDt               is null
                    and A.PAYMENT_CONFIRM_DT <= nvl(:paymentDt, A.PAYMENT_CONFIRM_DT)
                        then 'Y'
              end = 'Y'

       union all
       select A.COMPANY_CD
            , A.BIZ_UNIT_CD
            , H.RCP_PAY_NO
            , A.DEMAND_NO
            , A.DEMAND_NO_SEQ
            , A.CREATED_DT
            , A.SLIP_NO
            , A.SLIP_LINE_NO
            , A.ACCOUNT_CD
            , A.PAYMENT_METHOD_CD
            , A.PAYMENT_CONFIRM_DT       as PAYMENT_DT
            , (
                  nvl2(:paymentDt, to_date(:paymentDt, 'YYYYMMDD'), sysdate)
                - to_date(A.PAYMENT_CONFIRM_DT, 'YYYYMMDD')
              )                          as DAYS
            , A.CUST_CD
            , A.TRADE_CURRENCY_CD
            , A.EVE_EXCHANGE_RATE        as TRADE_EXCHANGE_RATE
            , nvl(A.TRADE_AMT       , 0) as TRADE_AMT
            , nvl(A.EVE_CALC_AMT    , 0) as CALC_AMT
            , (
                  nvl(A.SETTLE_TRADE_AMT, 0)
                - nvl(H.SETTLE_TRADE_AMT, 0)
              )                          as SETTLE_TRADE_AMT
            , (
                  nvl(A.SETTLE_TRANS_AMT, 0)
                - nvl(H.SETTLE_TRANS_AMT, 0)
              )                          as SETTLE_TRANS_AMT
            , nvl(H.SETTLE_TRADE_AMT, 0) as PAY_TRADE_AMT
            , nvl(H.SETTLE_TRANS_AMT, 0) as PAY_TRANS_AMT
            , (
                  nvl(A.TRADE_AMT       , 0)
                - nvl(A.SETTLE_TRADE_AMT, 0)
              )                          as REMAIN_TRADE_AMT
            , (
                  nvl(A.CALC_AMT        , 0)
                - nvl(A.SETTLE_TRANS_AMT, 0)
              )                          as REMAIN_TRANS_AMT
            , A.REMARKS
            , nvl
              (
                  A.DEPT_CD
                , (
                      select SA.DEPT_CD
                        from TB_CM_EMP SA
                       where SA.COMPANY_CD = A.COMPANY_CD
                         and SA.EMP_NO     = A.CHARGE_EMP_NO
                  )
              ) as DEPT_CD
            , A.CHARGE_EMP_NO
            , A.PURCHASE_DUTY_TP
            , A.SALES_DUTY_TP
            , A.FA_NO
            , A.FA_PROC_TP
            , A.CREDIT_CARD_CD
            , A.PROJECT_CD
            , A.UPPER_DEMAND_NO_SEQ
         from TB_FI_REVPAY_SETTLE_DIVIDE  A
         ,    TB_FI_REVPAY_SETTLE_HISTORY H
         ,    V_CONFIG                    V
        where  1 = 1
          and A.COMPANY_CD          = H.COMPANY_CD
          and A.SLIP_NO             = H.SLIP_NO
          and A.SLIP_LINE_NO        = H.SLIP_LINE_NO
          and A.DEMAND_NO           = H.DEMAND_NO
          and A.DEMAND_NO_SEQ       = H.DEMAND_NO_SEQ
          and H.COMPANY_CD          = :companyCd
          and H.RCP_PAY_NO          = :rcpPayNo
          and A.ACCOUNT_CD         in (
                                          select ATTRIBUTE1
                                            from TB_CM_CODE_VALUE
                                           where COMPANY_CD  = :companyCd
                                             and CLASS_CD    = ( select CLASS_CD   from V_ACCOUNT )
                                             and ATTRIBUTE2 <> ( select ATTRIBUTE2 from V_ACCOUNT )
                                      )
   ) A
  ,    TB_FI_ACCOUNT_COMPANY B
  ,    TB_CM_CUSTOMER        C
  ,    TB_CM_DEPT            D
  ,    TB_CM_EMP             E
  ,    TB_FI_CREDITCARD      F
  ,    TB_CM_EMP             G
--  ,    TB_SO_DEMAND          H
  ,    V_PROJECT_INFO        P

 where 1=1
   and A.COMPANY_CD     = B.COMPANY_CD    (+)
   and '1'              = B.GAAP_CD       (+)
   and A.ACCOUNT_CD     = B.ACCOUNT_CD    (+)

   and A.COMPANY_CD     = C.COMPANY_CD    (+)
   and A.CUST_CD        = C.CUST_CD       (+)

   and A.COMPANY_CD     = D.COMPANY_CD    (+)
   and A.DEPT_CD        = D.DEPT_CD       (+)

   and A.COMPANY_CD     = E.COMPANY_CD    (+)
   and A.CHARGE_EMP_NO  = E.EMP_NO        (+)

   and A.COMPANY_CD     = F.COMPANY_CD    (+)
   and A.CREDIT_CARD_CD = F.CREDIT_CARD_CD(+)

   and F.COMPANY_CD     = G.COMPANY_CD    (+)
   and F.CARD_OWNER     = G.EMP_NO        (+)

--   and A.COMPANY_CD     = H.COMPANY_CD    (+)
--   and A.DEMAND_NO      = H.DEMAND_NO     (+)

   and A.COMPANY_CD     = P.COMPANY_CD    (+)
   and A.PROJECT_CD     = P.PROJECT_CD    (+)

 order by A.PAYMENT_DT, A.DEMAND_NO