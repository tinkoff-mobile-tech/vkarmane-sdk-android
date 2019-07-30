# VKarmaneSDK

VKarmaneSDK - это библиотека, в которой содержатся инструменты упрощающие интеграцию функциии "Заполнить из ВКармане".

### Требования

Для работы VKarmaneSDK необходим Android версии 4.1 и выше (API level 16).

### Как подключить к проекту?

VKarmaneSDK распостраняется через Maven Central. Для подключения необходимо добавить в build.gradle:

```groovy
implementation "ru.tinkoff.vkarmane:sdk:$latestVersion"
```


### Подготовка к работе
Для того что бы использовать функцию "Заполнить из ВКармане" в production необходимо, что бы url схема вашего приложения, была добавлена в список доверенных, который содержится в нашем приложении. По вопросам добавления, можно связаться с нами написав на vkarmane-sdk@tinkoff.ru.

### Примеры использования SDK

#### Проверка установки ВКармане
```kotlin
    if (VKarmaneSDK.isAppInstalled(context)) {
        showVkarmaneButton()
    } else {
        hideVkarmaneButton()
    }
```
#### Схема запроса и получения данных документов
1. В Вашем приложении генерируется пара ключей (предпочтительно выполнять эту операцию асинхронно, т.к. она достаточно тяжелая и может кратковременно блокировать интерфейс).
2. Вызывается метод создания ссылки для перехода во ВКармане с интересующими параметрами.
3. Выполняется переход по ссылке.
4. Обрабатывается полученный от ВКармане URL, в котором расшифровываются данные изначально сгенерированным ключом.

#### Пример получения паспортных данных пользователя:
```kotlin
    // 1. Создаем и сохраняем ключи
    val keys = VKarmaneSDK.generateKeys()

    // 2. Создаем ссылку
    val vkarmaneLink = VKarmaneSDK.getDocumentsLinkBuilder()
            .setMultiChoice(multiChoice.isChecked)
            .setKinds(docs)
            .setXSource("Vkarmane Test")
            .setXCancelLink("vkarmanesdkexample://vkarmane-sdk-cancel")
            .setXErrorLink("vkarmanesdkexample://vkarmane-sdk-error")
            .setXSuccessLink("vkarmanesdkexample://vkarmane-sdk-success")
            .setPublicKey(keys.publicKey)
            .build()

    // 3. Переходим
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse(vkarmaneLink)
    startActivity(i)

    // 4. Получаем и расшифровываем данные изначально сгенерированным `keys.privateKey`
    intent.data?.let { uri ->
        val privateKey = keys.getPrivateKey()
        json = VKarmaneSDK.getJsonFromLink(uri, privateKey)
    }

```

### Описание протокола
Протокол взаимодействия реализованный в SDK, основан на [x-callback-url](http://x-callback-url.com/). В общем виде deeplink для запуска ВКармане выглядит следующем образом:
```
vkarmaneapp://x-callback-url/v2/<action>?<action-params>&<x-callback-params>
```
#### Доступные действия (`action`)
Название | Описание | 
-------- | -------- |
get_documents | Предложить пользователю выбрать один (или несколько) документов с типами указанными в `action-params`

#### Доступные параметры действий (`action-params`)
action | Название | Описание | Тип | Обязательность | Значение по-умолчанию |
------ | -------- | -------- | --- | -------------- | --------------------- |
get_documents | kinds | Список типов документов | VKarmaneSDK.DocumentKind | Да | -
get_documents | publicKey | Публичный ключ RSA-2048, закодированный в Base64 | String | Да | -
get_documents | isMultichoice | Переключение режимов выбора документа, если передан `true` то пользователь может выбрать несколько документов | Boolean | Нет | `false`

#### Параметры для запуска ВКармане `x-callback-params`

Название | Описание | 
-------- | -------- |
x-source | Название стороннего приложения | 
x-success | Deeplink для вызова стороннего приложения с данными выбранных пользователем документов, в качестве параметра `data`, в этот deeplink будет переден url encoded JSON c содержимым документа, без фото |
x-error | Deeplink для вызова стороннего приложения в случае возникновения ошибки. В качестве параметра `code` в ссылку добавится параметр code содержащий информацию об ошибке, см. ниже возможные коды ошибок. |
x-cancel | Deeplink для вызова стороннего приложения, в случае отмены пользователем опрерации |

Все перечисленные параметры обязательны

#### Коды ошибок (`code`)
Значение | Описание | 
-------- | -------- |
1 | Полученный deeplink приложением ВКармане не поддерживается
2 | Некорректный action
3 | Некорректные x-callback-params
4 | Некорректные action-params
5 | Во время обработки deeplink в приложении ВКармане произошла ошибка
6 | Пользователь не авторизован во ВКармане
7 | Ошибка криптографии

Для обеспечения безопасности пользователя **при возникновении ошибки с кодом 3 процесс прерывается в приложении ВКармане**, с сообщением, показывающим, что были переданы неверные параметры.

#### Поддерживаемые типы документов (`kinds`)

Значение | Описание | 
-------- | -------- |
RusNationalID | Паспорт РФ |
RusDriversLic | ВУ |
RusDriversLicOld | ВУ старого образца |
RusDriversLicOldest | ВУ самого старого образца |
RusInternationalID | Загранпаспорт |
RusSNILS | СНИЛС |
RusINN | ИНН |
RusBirthCert | Свидетельство о рождении |
RusMedIns | Полис ОМС |
RusVehicleRegID | СТС |
RusPTS | ПТС |
RusOSAGO | ОСАГО |
RusKASKO | КАСКО |
RusMilitaryCard | Военный билет |
BankCard | Банковская карта |


### Sample
Содержит пример интеграции "Заполнить из ВКармане".

### Поддержка 

* Все возникающие вопросы можно направить на vkarmane-sdk@tinkoff.ru
* Баги и feature-реквесты направлять в раздел issues

### Справочник значений

* `sexCode`

Значение | Описание | 
-------- | -------- |
0 | Жен. |
1 | Муж. |

* `citizenshipCountryCode`

Значение | Название | 
-------- | -------- |
0 | Афганистан |
1 | Албания |
2 | Алжир |
3 | Андорра |
4 | Ангола |
5 | Антигуа и Барбуда |
6 | Аргентина |
7 | Армения |
8 | Австралия |
9 | Австрия |
10 | Азербайджан |
11 | Багамы |
12 | Бахрейн |
13 | Бангладеш |
14 | Барбадос |
15 | Белоруссия |
16 | Бельгия |
17 | Белиз |
18 | Бенин |
19 | Бутан |
20 | Боливия |
21 | Босния и Герцеговина |
22 | Ботсвана |
23 | Бразилия |
24 | Бруней |
25 | Болгария |
26 | Буркина-Фасо |
27 | Бирма |
28 | Бурунди |
29 | Камбоджа |
30 | Камерун |
31 | Канада |
32 | Кабо-Верде |
33 | ЦАР |
34 | Чад |
35 | Чили |
36 | КНР |
37 | Колумбия |
38 | Коморы |
39 | ДР Конго |
40 | Коста-Рика |
41 | Кот-д’Ивуар |
42 | Хорватия |
43 | Куба |
44 | Кипр |
45 | Чехия |
46 | Дания |
47 | Джибути |
48 | Доминика |
49 | Доминиканская Республика |
50 | Восточный Тимор |
51 | Эквадор |
52 | Египет |
53 | Сальвадор |
54 | Экваториальная Гвинея |
55 | Эритрея |
56 | Эстония |
57 | Эфиопия |
58 | Фиджи |
59 | Финляндия |
60 | Франция |
61 | Габон |
62 | Гамбия |
63 | Грузия |
64 | Германия |
65 | Гана |
66 | Греция |
67 | Гренада |
68 | Гватемала |
69 | Гвинея |
70 | Гвинея-Бисау |
71 | Гаити |
72 | Гондурас |
73 | Гонконг |
74 | Венгрия |
75 | Исландия |
76 | Индия |
77 | Индонезия |
78 | Иран |
79 | Ирак |
80 | Ирландия |
81 | Израиль |
82 | Италия |
83 | Ямайка |
84 | Япония |
85 | Иордания |
86 | Казахстан |
87 | Кения |
88 | Кирибати |
89 | КНДР |
90 | Республика Корея |
91 | Республика Косово |
92 | Кувейт |
93 | Киргизия |
94 | Лаос |
95 | Латвия |
96 | Ливан |
97 | Лесото |
98 | Либерия |
99 | Ливия |
100 | Лихтенштейн |
101 | Литва |
102 | Люксембург |
103 | Македония |
104 | Мадагаскар |
105 | Малави |
106 | Малайзия |
107 | Мальдивы |
108 | Мали |
109 | Мальта |
110 | Маршалловы Острова |
111 | Мавритания |
112 | Маврикий |
113 | Мексика |
114 | Микронезия |
115 | Молдавия |
116 | Монако |
117 | Монголия |
118 | Черногория |
119 | Марокко |
120 | Мозамбик |
121 | Намибия |
122 | Науру |
123 | Непал |
124 | Нидерланды |
125 | Новая Зеландия |
126 | Никарагуа |
127 | Нигер |
128 | Нигерия |
129 | Норвегия |
130 | Оман |
131 | Пакистан |
132 | Палау |
133 | Палестинская национальная администрация |
134 | Панама |
135 | Папуа |
136 | Парагвай |
137 | Перу |
138 | Филиппины |
139 | Польша |
140 | Португалия |
141 | Катар |
142 | Румыния |
143 | Российская Федерация |
144 | Руанда |
145 | Сент-Китс и Невис |
146 | Сент-Люсия |
147 | Сент-Винсент и Гренадины |
148 | Самоа |
149 | Сан-Марино |
150 | Сан-Томе и Принсипи |
151 | Саудовская Аравия |
152 | Сенегал |
153 | Сербия |
154 | Сейшельские Острова |
155 | Сьерра-Леоне |
156 | Сингапур |
157 | Синт-Мартен |
158 | Словакия |
159 | Словения |
160 | Соломоновы Острова |
161 | Сомали |
162 | ЮАР |
163 | Южный Судан |
164 | Испания |
165 | Шри-Ланка |
166 | Судан |
167 | Суринам |
168 | Швеция |
169 | Швейцария |
170 | Сирия |
171 | Таджикистан |
172 | Танзания |
173 | Таиланд |
174 | Того |
175 | Тонга |
176 | Тринидад и Тобаго |
177 | Тунис |
178 | Турция |
179 | Туркмения |
180 | Тувалу |
181 | Уганда |
182 | Украина |
183 | ОАЭ |
184 | Великобритания |
185 | США |
186 | Уругвай |
187 | Узбекистан |
188 | Вануату |
189 | Ватикан |
190 | Венесуэла |
191 | Вьетнам |
192 | Йемен |
193 | Замбия |
194 | Зимбабве |

* `RusDriversLic.categories.code`

Значение | Описание | 
-------- | -------- |
0 | "A" |
1 | "B" |
2 | "C" |
3 | "D" |
4 | "BE" |
5 | "CE" |
6 | "DE" |
7 | "Трамвай" |
8 | "Троллейбус" |
9 | "А1" |
10 | "B1" |
11 | "C1" |
12 | "D1" |
13 | "C1E" |
14 | "D1E" |
16 | "AM" |


* `RusKASKO.driversCode`

Значение | Описание | 
-------- | -------- |
0 | Без ограничений |
1 | 1 |
2 | 2 |
3 | 3 |
4 | 4 |

* `RusKASKO.instalment.code`

Значение | Описание | 
-------- | -------- |
0 | "Сразу все" |
1 | "Полгода (два взноса)" |
2 | "Поквартально (четыре взноса)" |
3 | "Сейчас + остаток через 3 мес." |

* `RusKASKO.paymentOrderCode`

Значение | Описание | 
-------- | -------- |
0 | Единовременно |
1 | В рассрочку |

* `RusKASKO.surveyor`

Значение | Описание | 
-------- | -------- |
false | Не выбраны сюрвейерские услуги |
true | Выбраны сюрвейерские услуги |

* `RusVehicleRegID.vehicle.categoryCode`

Значение | Описание | 
-------- | -------- |
0 | A |
1 | B |
2 | C |
3 | D |
4 | "Прицеп" |

* `reserve.categoryCode`

Значение | Описание | 
-------- | -------- |
0 | Категория запаса 1 |
1 | Категория запаса 2 |

* `reserve.groupCode`

Значение | Описание | 
-------- | -------- |
0 | РА |
1 | ВМФ |

* `suitabilityCode`

Значение | Описание | 
-------- | -------- |
0 | А - годен без ограничений |
1 | Б - годен с ограничениями |
2 | В - годен только в условиях всеобщей мобилизации (в военное время) |
3 | Г - временно не годен |
4 | Д - не годен |


### JSON модели возвращаемых данных
#### Паспорт РФ (RusNationalID)
```json
{
    "kind": "RusNationalID",
    "serial": "9608",
    "number": "437101",
    "dates": {
      "delivery": "2008-12-09"
    },
    "code": "430-040",
    "deliveredBy": "ТП №25 ОТДЕЛА УФМС ПО САНКТ-ПЕТЕРБУРГУ И ЛЕНИНГРАДСКОЙ ОБЛ. В КИРОВСКОМ Р-НЕ ГОР. САНКТ-ПЕТЕРБУРГА",
    "person": {
      "firstName": "Павел",
      "middleName": "Владимирович",
      "lastName": "Федоров",
      "birthDate": "1963-11-12",
      "birthPlace": "с. БОРИСОВКА, ТУРКИНСТАНСКОГО Р-НА, ЧЕМКЕНТСКОЦ ОБЛ. ",
      "sexCode": 0
    },
    "registration": {
      "date": "2016-10-20",
      "department": "ТП N2 МЕЖРАЙОННОГО ОУФМС РОССИИ ПО МОСКОВСКОЙ ОБЛ. В ГОРОДСКОМ ПОСЕЛЕНИИ ЛЮБЕРЦЫ",
      "residence": {
        "humanReadable": "Свердловск. обл, Железногорск, Ленина 126/2, 22",
        "region": "Свердловская область",
        "district": "Устиновский район",
        "point": "Гор. Железногорск",
        "street": "Ул. Ленина",
        "building": "14б",
        "corpus": "2",
        "stroenie": "4",
        "apartment": "22"
        }
    }
}
  
```
#### Загранпаспорт РФ (RusInternationalID)
```json
{
    "kind": "RusInternationalID",
    "number": "722836941",
    "dates": {
      "delivery": "2007-02-20",
      "expiration": "2017-02-20"
    },
    "deliveredBy": "МВД-335",
    "person": {
      "firstName": "Александр",
      "middleName": "Русланович",
      "lastName": "Куликов",
      "firstNameEn": "Alexander",
      "lastNameEn": "Kulikov",
      "birthDate": "1990-12-31",
      "birthPlace": "село Кукуево",
      "citizenshipCountryCode": 42,
      "sexCode": 1
    }
}
```
#### Водительское удостоверение нового образца (RusDriversLic)
```json
{
    "kind": "RusDriversLic",
    "serial": "1234",
    "number": "458980",
    "dates": {
      "delivery": "2008-11-02",
      "expiration": "2018-11-02"
    },
    "categories": [
        {
        "dates": {
            "delivery": "1998-09-10",
            "expiration": "2028-09-10"
        },
        "code": 42,
        "notes": "Очки, линзы"
        }
    ],
    "deliveredBy": "ГИБДД 4201",
    "deliveredByEn": "GIBDD 4201",
    "notes": "Очки, линзы обязательны",
    "person": {
      "firstName": "Юрий",
      "middleName": "Алексеевич",
      "lastName": "Ульященко",
      "firstNameEn": "Yuriy",
      "middleNameEn": "Alekseevich",
      "lastNameEn": "Ulyashcenko",
      "birthDate": "1984-03-14",
      "birthPlace": "Кемеровская обл.",
      "birthPlaceEn": "Kemerovskaya obl.",
      "livePlace": "Кемеровская обл.",
      "livePlaceEn": "Kemerovskaya obl."
    }
  }
```
#### Водительское удостоверение старого образца (RusDriversLicOld)
```json
{
    "kind": "RusDriversLicOld",
    "category": {
        "A": true,
        "B": false,
        "C": true,
        "D": false,
        "E": true
    },
    "person": {
        "lastNameEn":"Ulyashcenko",
        "lastName":"Ульященко",
        "firstName":"Юрий",
        "firstNameEn":"Yuriy",
        "middleName":"Алексеевич",
        "birthDate":"1984-03-14",
        "birthPlace":"Кемеровская обл.",
        "livePlace":"Кемеровская обл."
    },
    "deliveredBy":"ГИБДД 4201",
    "dates": {
        "delivery":"1998-09-10",
        "expiration":"2020-09-10"
    },
    "notes": "example"
}
```
#### Водительское удостоверение самого старого образца (RusDriversLicOldest)
```json
{
    "kind": "RusDriversLicOldest",
    "category": {
        "A": true,
        "B": false,
        "C": true,
        "D": false,
        "E": true
    },
    "person": {
        "lastName": "Ульященко",
        "lastNameEn": "Ulyashcenko",
        "firstName": "Юрий",
        "firstNameEn": "Yuriy",
        "middleName": "Алексеевич",
        "birthDate": "1984-03-14",
        "birthPlace": "Кемеровская обл.",
        "birthPlaceEn": "Kemerovskaya obl.",
        "livePlace": "Кемеровская обл.",
        "livePlaceEn": "Kemerovskaya obl."
    },
    "deliveredBy": "ГИБДД 4201",
    "dates": {
        "delivery": "1998-09-10",
        "expiration": "2028-09-10"
    },
    "notes": "Очки, линзы обязательны"
}
```
#### СНИЛС (RusSNILS)
```json
{
    "kind": "RusSNILS",
    "number": "00187900429",
    "dates": {
      "delivery": "1998-03-19"
    },
    "person": {
      "firstName": "Александр",
      "middleName": "Владимирович",
      "lastName": "Владимиров",
      "birthDate": "1986-01-12",
      "birthPlace": "Оренбургская область, г. Орск"
    }
}
```
#### ИНН (RusINN)
```json
{
    "kind": "RusINN",
    "serial": "39",
    "number": "004664470",
    "dates": {
      "delivery": "2007-04-25"
    },
    "deliveredBy": "Межрайонной ИФНС России №13 по Ростовской области (МРИ №13 террит. участок 6150 по г. Новочеркасску, код 6150)",
    "inn": "742005900451",
    "person": {
      "firstName": "Артур",
      "middleName": "Вениаминович",
      "lastName": "Кутищев",
      "birthDate": "1997-05-15"
    }
}
```
#### Свидетельство о рождении (RusBirthCert)
```json
{
    "kind": "RusBirthCert",
    "serial": "VI-МЮ",
    "number": "497676",
    "dates": {
      "delivery": "2008-01-02"
    },
    "act": {
      "date": "2015-07-14",
      "recordNumber": "372"
    },
    "father": {
      "firstName": "Павел",
      "middleName": "Вениаминович",
      "lastName": "Калинкин",
      "citizenshipCountryCode": 42
    },
    "mother": {
      "firstName": "Елизавета",
      "middleName": "Тимофеевна",
      "lastName": "Калинкина",
      "citizenshipCountryCode": 42
    },
    "person": {
      "firstName": "Артур",
      "middleName": "Павлович",
      "lastName": "Калинкин",
      "birthDate": "2003-08-07",
      "birthPlace": "п. Боровской Мендыгаринского р-на Кустанайской обл. Республика Казахстан"
    },
    "registrationPlace": "отдел ЗАКС Калининского района г. Новосибирска управления по делам ЗАКС Новосибирской области"
  }
```
#### Медицинский полис (RusMedIns)
```json
{
    "kind": "RusMedIns",
    "number": "5947300848000179",
    "dates": {
      "expiration": "2017-02-20"
    },
    "letterHeadNumber": "01085451901",
    "person": {
      "firstName": "Галина",
      "middleName": "Александровна",
      "lastName": "Брунькина",
      "birthDate": "1980-02-21"
    }
}
```
#### СТС (RusVehicleRegID)
```json
{
    "kind": "RusVehicleRegID",
    "dates": {
      "delivery": "2008-01-02"
    },
    "code": "11450504",
    "deliveredBy": "МОГТОРЭР №4 ГИБДД ГУ МВД РФ по г.Москве",
    "notes": "Особые отметки",
    "owner": "Иванов Петр Григорьевич",
    "residence": {
      "humanReadable": "Ленинград, Голикова, 35, 0",
      "region": "Москва",
      "district": "Москва",
      "point": "Санкт-Петербург, settlement",
      "street": "УЛ ЛЕНИ ГОЛИКОВА",
      "building": "35",
      "corpus": "10",
      "stroenie": "2",
      "apartment": "000"
    },
    "serialAndNumber": "37НХ842827",
    "vehicle": {
      "type": "ЛЕГКОВОЙ УНИВЕРСАЛ",
      "body": "Z6FMXXESWMDC67089",
      "categoryCode": 1,
      "chassis": "XLRTE47XS0E575898",
      "color": "ЧЕРНЫЙ",
      "emptyWeight": "1300",
      "engine": {
        "number": "A671605",
        "capacity": "1345",
        "model": "B5254T",
        "power": "132.00/180"
      },
      "makeAndModel": "NISSAN ALMERA CLASSIC",
      "maxWeight": "1500",
      "passport": {
        "serial": "77TX",
        "number": "575939"
      },
      "sign": "A236AO92",
      "vin": "XTAGFL120GY001607",
      "year": 2006
    }
}
```
#### ПТС (RusPTS)
```json
{
    "kind": "RusPTS",
    "approvement": {
      "number": "123.FA.3123123",
      "approvedDate": "1990-12-31",
      "deliveredBy": "Таможня Пермского края"
    },
    "dealer": {
      "address": "Москва, ул. Ленина, д.4",
      "deliveredAddress": "Москва, Замкадье ул.Самая-дальняя д.6",
      "deliveredBy": "ГИБДД",
      "deliveredDate": "1990-12-31",
      "notes": "Особые заметки",
      "organization": "Тойота-Центр Москва"
    },
    "export": {
      "country": "Япония",
      "restrictions": "Нельзя управлять в нетрезвом виде",
      "serialAndNumber": "УК123123123"
    },
    "notes": "Заметки",
    "owner": {
      "name": "Ларионов Андрей Петрович",
      "address": "Томск, ул. Московская д. 1",
      "notes": "Заметка",
      "ownershipDocument": "Договор купли-продажи",
      "saleDate": "1990-12-31"
    },
    "serialAndNumber": "70ТТ384938",
    "sts": {
      "serial": "22АА",
      "number": "123456",
      "deliveredBy": "ГИБДД г. Томска",
      "document": "Документ",
      "registrationDate": "1990-12-31",
      "sign": "в234ра 70"
    },
    "vehicle": {
      "type": "Легковые универсал",
      "body": "GTR3857HTRE3",
      "categoryCode": 1,
      "chassis": "12345",
      "color": "Черный",
      "companyAndCountry": "Toyota, Japan",
      "emptyWeight": "1300",
      "engine": {
        "type": "бензиновый",
        "capacity": "1345",
        "modelAndNumber": "VHB 200, 173678",
        "power": "62.5/85"
      },
      "makeAndModel": "Toyota Land Cruiser 200",
      "maxWeight": "1500",
      "vin": "XTAGFL120GY001607",
      "year": 2006
    }
  }
```
#### ОСАГО (RusOSAGO)
```json
{
    "kind": "RusOSAGO",
    "serial": "ЕЕЕ",
    "number": "2000759617",
    "dates": {
      "delivery": "2008-12-09"
    },
    "company": {
      "name": "ООО Ладастрах",
      "address": "197376, г. СПб, Аптекарская набережная, д.12, 2 эт",
      "phone": "+78127777901"
    },
    "contract": {
      "cost": "150000",
      "period": {
        "begin": "2010-11-15",
        "beginTime": "22:30",
        "end": "2012-11-15",
        "endTime": "22:30"
      }
    },
    "notes": "Заметки",
    "person": {
      "firstName": "Дмитрий",
      "middleName": "Анатольевич",
      "lastName": "Решетнев"
    },
    "vehicle": {
      "makeAndModel": "Toyota Land Cruiser 200",
      "passport": {
        "serial": "77УВ",
        "number": "395249"
      },
      "sign": "Х676МК197",
      "vin": "Z8TXTGF3WHM007672"
    }
}
```
#### КАСКО (RusKASKO)
```json
{
    "kind": "RusKASKO",
    "serial": "07305/046",
    "number": "02-069",
    "dates": {
      "delivery": "2008-12-09"
    },
    "company": {
      "name": "ООО \"Ладастрах\"",
      "email": "company@example.com",
      "phone": "+79263141593"
    },
    "contract": {
      "cost": "150000",
      "period": {
        "begin": "2010-11-15",
        "end": "2012-11-15"
      }
    },
    "driversCode": 0,
    "instalment": {
      "code": 1,
      "period": {
        "begin": "2010-11-15"
      }
    },
    "insurance": {
      "civilLiability": "100503",
      "damage": "100501",
      "optionalEquipment": "100504",
      "theft": "100502"
    },
    "notes": "Временное разрешение выдано",
    "paymentOrderCode": 1,
    "person": {
      "serial": "0301",
      "number": "523812",
      "firstName": "Дмитрий",
      "middleName": "Анатольевич",
      "lastName": "Решетнев",
      "birthDate": "1990-12-31",
      "inn": "23492538593",
      "issueDate": "1990-12-31"
    },
    "residence": {
      "humanReadable": "г. Нижний Устюг, ул. Ленина д.4 кв. 20",
      "region": "Москва",
      "district": "Москва",
      "point": "Москва",
      "street": "Ленина",
      "building": "10",
      "corpus": "2",
      "stroenie": "4",
      "apartment": "3"
    },
    "surveyor": false,
    "vehicle": {
      "engine": {
        "power": "100"
      },
      "makeAndModel": "Toyota Land Cruiser 200",
      "passport": {
        "serial": "77УВ",
        "number": "395249"
      },
      "sign": "Х676МК197",
      "vin": "Z8TXTGF3WHM007672",
      "year": 2006
    }
}
```
#### Военный билет (RusMilitaryCard)
```json
{
    "kind": "RusMilitaryCard",
    "name": "Военный билет",
    "serial": "AA",
    "number": "5461728",
    "dates": {
      "delivery": "1999-05-12"
    },
    "commissariat": "Войковский ОВК г. Рязань",
    "commissionName": "Наименования призывной комиссии",
    "deliveredBy": "Комиссариатом Приморского Края",
    "person": {
      "firstName": "Андрей",
      "middleName": "Александрович",
      "lastName": "Иванов",
      "birthDate": "1990-12-31",
      "birthPlace": "г. Москва"
    },
    "rank": {
      "awardedBy": "Военный комиссар Останкинского района СВАО г. Москвы",
      "order": {
        "number": "398",
        "date": "1999-07-21"
      },
      "rank": "Старший лейтенант"
    },
    "reserve": {
      "categoryCode": 1,
      "groupCode": 0,
      "staff": "Командный"
    },
    "suitabilityCode": 1,
    "vus": {
      "number": "659",
      "code": "659941A",
      "specialty": "Военная разведка"
    }
}
```
#### Банковская карта (BankCard)
```json

{
    "kind": "BankCard",
    "cardholder": "OLEG TINKOFF",
    "expiryDate": "12/18",
    "number": "4111111111111111"
}
```

