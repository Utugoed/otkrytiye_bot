patterns:
    $pin_ptrn = (парол*|пин*|pin*|код*|*пинкод*)
    $app_ptrn = прилож*

theme: /

    init: 
        bind("preMatch", function($context) {
            $context.request.query = $context.request.query.replace(/[^a-zA-Zа-яА-Я]/g, ' ');
            log("TEXTCONTEXT - " + $context.textContext);
        });

    state: PasswordEdit
        q!: {* $pin_ptrn *}
        a: Fuck yeah

    state: Echo
        event!: noMatch
        a: You said: {{$context.request.query}}

    state: Match
        event!: match
        a: {{$context.intent.answer}}
