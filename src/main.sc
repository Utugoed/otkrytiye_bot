patterns:
    $pin_ptrn = (парол*|пин*|pin*|код*|*пинкод*)
    $creds_ptrn = (код доступ*|логин и пароль)
    $app_ptrn = (для * приложения|к приложению|на приложении|приложения)
    $prsnl_acc_ptrn = ((интернет|моб*|онлайн) банка|личн* кабинета)
    $entrance_ptrn = вход*
    $card_ptrn = карт*
    $atm_ptrn = банкомат*
    
    $qr_ptrn = (qr*|куар*|кьюар*)
    $sms_ptrn = (sms|смс)
    $error_ptrn = (ошибк*|что то пошло не так|не ([у]дает*|меняет*))


theme: /

    init:
        var SESSION_TIMEOUT_MS = 60000; // Минута

        bind("preMatch", function($context) {
            $context.request.query = $context.request.query.replace(/[^a-zA-Zа-яА-Я]/g, ' ');
            log("TEXTCONTEXT - " + $context.textContext);
        });
        
        bind("postProcess", function($context) {
            $context.session.lastActiveTime = $jsapi.currentTime();
        });
    
        bind("preProcess", function($context) {
            if ($context.session.lastActiveTime) {
                var interval = $jsapi.currentTime() - $context.session.lastActiveTime;
                if (interval > SESSION_TIMEOUT_MS) $jsapi.startSession();
            }
        });

    state: Garbage
        q!: {* $qr_ptrn *} $weight<+0.4>
        q!: {* $sms_ptrn *} $weight<1.1+0.4>
        q!: {* $error_ptrn * [$error_ptrn] *} $weight<1.1+0.4>
        a: Garbage request

    state: PasswordEdit
        q!: {* $pin_ptrn *}
        script:
            $reactions.answer("Здравствуйте!");
            $reactions.answer(
                'Сейчас расскажу порядок действий.\n' + 
                'Выберите, что именно планируете сделать:\n' +
                '1. Поменять пароль для входа в приложение.\n' +
                '2. Поменять PIN-код от карты.\n' +
                'Пожалуйста, отправьте цифру, соответствующую вашему выбору.'
            );
        
        state: CardPasswordEdit
            q!: * {$pin_ptrn * $card_ptrn} *
            q!: * {$atm_ptrn * $pin_ptrn} *
            q: (2|карта)
            script:
                $reactions.answer(
                    'Это можно сделать в приложении:\n' +
                    '1. На экране "Мои деньги" в разделе "Карты" нажмите на нужную.\n' +
                    '2. Выберите вкладку "Настройки".\n' +
                    '3. Нажмите "Сменить пин-код".\n' +
                    '4. И введите комбинацию, удобную вам.\n' +
                    '5. Повторите ее.'
                );
                
                $reactions.answer(
                    'И все готово!\n' +
                    'Пин-код установлен, можно пользоваться. J '
                );

                $reactions.answer("Приятно было пообщаться. Всегда готов помочь вам снова J");
        
        state: AppPasswordEdit
            q!: * {$entrance_ptrn * $pin_ptrn} * $weight<1.2>
            q!: * {$pin_ptrn * ($app_ptrn|$prsnl_acc_ptrn)} *
            q!: {* $creds_ptrn *}
            q: (1|приложение)
            scriptEs6:
                $jsapi.stopSession();
                
                $conversationApi.sendTextToClient(
                    'Смена пароля от приложения возможна несколькими способами:' +
                    '1. на экране "Профиль" выберите "Изменить код входа в приложение".\n' +
                    '2. введите SMS-код.\n' +
                    '3. придумайте новый код для входа в приложение и повторите его.'
                );
                
                await new Promise(resolve => setTimeout(resolve, 2000));
                
                $reactions.answer(
                    'Либо нажмите на кнопку "Выйти" на странице ввода пароля для входа в приложение.\n\n' +
                    'После чего нужно будет заново пройти регистрацию:\n' +
                    '1. ввести полный номер карты (если оформляли ранее, иначе номер телефона и дату рождения),\n' +
                    '2. указать код из смс-код,\n' +
                    '3. придумать новый пароль для входа.'
                );
                
                setTimeout(
                    () => $conversationApi.sendTextToClient(
                        "Приятно было пообщаться. Всегда готов помочь вам снова J"
                    ),
                    2000
                );


    state: Echo
        event!: noMatch
        a: You said: {{$context.request.query}}

    state: Match
        event!: match
        a: {{$context.intent.answer}}
