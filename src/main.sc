patterns:
    $pin_ptrn = (парол*|пин*|pin*|код*|*пинкод*)
    $creds_ptrn = (код доступ*|логин и пароль)
    $app_ptrn = (для * приложения|к приложению|на приложении|приложения)
    $prsnl_acc_ptrn = ((интернет|моб*|онлайн) банка|личн* кабинета)
    $entrance_ptrn = вход*
    $card_ptrn = карт*
    $atm_ptrn = банкомат*
    
    $qr_ptrn = (*qr*|*куар*|*кьюар*)

theme: /

    init: 
        bind("preMatch", function($context) {
            $context.request.query = $context.request.query.replace(/[^a-zA-Zа-яА-Я]/g, ' ');
            log("TEXTCONTEXT - " + $context.textContext);
        });

    state: CardPasswordEdit
        q!: * {$pin_ptrn * $card_ptrn} *
        q!: * {$atm_ptrn * $pin_ptrn} *
        a: Card yeah
    
    state: AppPasswordEdit
        q!: * {$entrance_ptrn * $pin_ptrn} * $weight<1.2>
        q!: * {$pin_ptrn * ($app_ptrn|$prsnl_acc_ptrn)} *
        q!: {* $creds_ptrn *}
        a: App yeah

    state: PasswordEdit
        q!: {* $pin_ptrn *}
        a: Fuck yeah

    state: Garbage
        q!: * $qr_ptrn *
        a: Garbage request

    state: Echo
        event!: noMatch
        a: You said: {{$context.request.query}}

    state: Match
        event!: match
        a: {{$context.intent.answer}}
