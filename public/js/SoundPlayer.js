function SOUND_PLAYER_SLIDER( settings ){

    var start       = 0,
        difference  = 0,
        started     = false,
        width       = 0;

    function prevent( event ){
        event.preventDefault();
        event.stopPropagation();
    }

    function setSliderWidth( event ){

        difference = event.clientX - start;
        if( !difference ){
            difference = event.clientX - settings.sliderHolder.offset().left
        }else {
            difference += width;
        }
        difference = difference * 100 / settings.sliderContainer.width();
        difference = ( difference < settings.min ) ? settings.min : ( difference > settings.max ) ? settings.max : difference;
        settings.sliderHolder.css({ width : difference + '%' });
        settings.sliderContainer.data({ 'value':difference });

        var isMoved = !!event.data && event.data.isMoved;
        isMoved = ( isMoved ) ? settings.slideOnMove : true;

        isMoved && !!settings.callback && typeof settings.callback === 'function' && settings.callback( difference.toFixed(2) );
    }

    function mouseDown( event ){

        started = true;
        prevent( event );
        settings.sliderContainer.addClass( 'active' );
        start = event.clientX;
        event.data = ( !!event.data ) ? $.extend(event.data, {isMoved:true}) : {isMoved:true};
        setSliderWidth( event );
        width = settings.sliderHolder.width();
    }

    function mouseMove( event ){

        if( !started ) return;
        prevent( event );
        event.data = ( !!event.data ) ? $.extend(event.data, {isMoved:true}) : {isMoved:true};
        setSliderWidth( event );
    }

    function mouseUp( event ){

        if( !started ) return;
        prevent( event );
        setSliderWidth( event );
        started = false;
        settings.sliderContainer.removeClass( 'active' );
    }


    if( !arguments.length ) return;
    settings = arguments[0];

    settings.sliderContainer.bind({
        'mousedown' : mouseDown
    });

    $( window ).bind({
        'mousemove' : mouseMove,
        'mouseup'   : mouseUp
    });
}


soundManager.url = '/public/swfs/';
soundManager.flashVersion = 9;
soundManager.useFlashBlock = false;
soundManager.useHighPerformance = true;
soundManager.wmode = 'transparent';
soundManager.useFastPolling = true;


function LS_SOUND_PLAYER( settings ){

    this.url = settings.url;
    this.id  = settings.id;

    this.outerTimeline = null;
    this.timeline      = null;

    this.activeData  = null;
    this.activeTrack = null;

    this.createSoundManagerObject( $( '<ul class="tracks"></ul>' ) );
}

LS_SOUND_PLAYER.prototype.setVolume = function( id ){

    var value = $( '.player.active .volume ').data( 'value' );
    value = ( !!value ) ? value : 50;
    soundManager.setVolume( id, value );
};

LS_SOUND_PLAYER.prototype.calculateEstimateDuration = function( track ){

    var duration = ( Math.abs(((!!track.position && track.position != track.duration )?track.position:0) - track.duration) / ( 1000*60 ) ).toFixed(2).split('.');

    duration[0] = (duration[0].length == 1) ? '0' + duration[0] : duration[0];

    duration[1] = Math.floor( (duration[1]*60/100).toPrecision(2) );
    duration[1] = (duration[1]-10 < 0 || !duration[1] ) ? '0' + duration[1] : duration[1];

    return duration;
};

LS_SOUND_PLAYER.prototype.createSoundManagerObject = function( tracksSkeleton ){

    var consumer_key = "7669adc727f5f154c47f8dd3b75c054c",
        _CLASS       = this;

    function createSound( track ){
        // If your playlist is private, make sure your url includes the secret token you were given.
        var sound           = null,
            duration,
            timeStamp,
            currentTime,
            url             = track.stream_url;

        url += ( !(url.indexOf("secret_token") + 1) ) ? '?' : '&';
        url = url + 'consumer_key=' + consumer_key;

        // Creating the streaming sound.
        sound = soundManager.createSound({

            id: 'track_' + track.id,
            url: url,

            onload: function(){},

            onplay: function() {
                // Set volume rate.
                _CLASS.setVolume( this.id );
                timeStamp = new Date().getTime();

            },
            onresume: function() {
                // Set volume rate.
                _CLASS.setVolume( this.id );
            },

            onpause: function() {},
            onstop: function(){
                this.position = this.duration;

                var isActive = !_CLASS.timeline.is( '.active' );
                if( isActive ){
                    _CLASS.outerTimeline.css({ 'width': '0%' });
                }

                var current = _CLASS.calculateEstimateDuration( this );
                _CLASS.activeTrack.find( '.duration' ).html( current[0] + '&#58;' + current[1] );
            },

            onfinish: function() {
                var current = _CLASS.calculateEstimateDuration( this );
                _CLASS.activeTrack.find( '.duration').html( current[0] + '&#58;' + current[1] );
                _CLASS.nextTrack();
            },
            whileplaying: function(){

                function toDoEverySecond( _this ){

                    var isActive = !_CLASS.timeline.is( '.active' );
                    if( isActive ){
                        _CLASS.outerTimeline.css({ 'width': (_this.position*100/_this.duration) + '%' });
                    }

                    if( !_CLASS.activeTrack ) return;

                    var current = _CLASS.calculateEstimateDuration( _this );

                    _CLASS.activeTrack.find( '.duration').html( current[0] + '&#58;' + current[1] );
                }
                currentTime = new Date().getTime();
                if( currentTime - timeStamp > 1000 ){
                    timeStamp = currentTime;
                    toDoEverySecond( this );
                }
            }

        });

        var current = _CLASS.calculateEstimateDuration( track );

        // Creating track list representation with inside data.
        $('<li><s></s><span class="title">' + track.title + '</span><time class="duration">' + current[0] + '&#58;' + current[1] + '</time></li>')
            .data({
                track : track,
                sound : sound
            })
            .appendTo( tracksSkeleton );
    }

    // Resolve the given url and get the full JSON-worth of data from SoundCloud regarding the playlist and the tracks within.
    $.getJSON('http://api.soundcloud.com/resolve?url=' + this.url + '&format=json&consumer_key=' + consumer_key + '&callback=?', function( playlist ){

        if( playlist.kind == 'playlist' ){
            $.each(playlist.tracks, function(index, track) {
                createSound( track );
            });
        }else if( playlist.kind == 'track' ){
            createSound( playlist );
        }

        _CLASS.createPlayerSkeleton( tracksSkeleton );

    });
};

LS_SOUND_PLAYER.prototype.createPlayerSkeleton = function( tracksSkeleton ){

    var playerSkeleton = $( '<div class="player">' +
                        '<table class="controls">' +
                            '<tbody>' +
                                '<tr>' +
                                    '<td class="timeline-container">' +
                                        '<div class="timeline">' +
                                            '<div class="inner"></div>' +
                                            '<div class="outer">' +
                                                '<s class="fingerPoint"></s>' +
                                            '</div>' +
                                        '</div>' +
                                    '</td>' +
                                    '<td class="volume-container">' +
                                        '<div class="volume">' +
                                            '<div class="inner"></div>' +
                                            '<div class="outer">' +
                                                '<s class="fingerPoint"></s>' +
                                            '</div>' +
                                        '</div>' +
                                    '</td>' +
                                '</tr>' +
                            '</tbody>' +
                        '</table>' +
                    '</div>').append( tracksSkeleton );

    this.embeddPlayer( playerSkeleton );
};

LS_SOUND_PLAYER.prototype.embeddPlayer = function( playerSkeleton ){

    var target = document.getElementById( this.id );

    if( !target ) return;

    this.setInitials( playerSkeleton );

    target.parentNode.insertBefore( playerSkeleton.get(0), target );
    target.parentNode.removeChild( target );
};

LS_SOUND_PLAYER.prototype.setInitials = function( playerSkeleton ){

    var _CLASS = this;

    _CLASS.outerTimeline = playerSkeleton.find( '.timeline .outer').eq(0);
    _CLASS.timeline      = playerSkeleton.find( '.timeline' ).eq(0);

    // // Likespot.ru: [ Dummy handler for preventing BootStrap Slide Toggle Posts behavior. ]
    playerSkeleton.bind( 'click', function( event ){

        event.preventDefault();
        event.stopPropagation();
    });

    playerSkeleton.find( '.tracks li').bind( 'click', function( event ){

        event.preventDefault();
        event.stopPropagation();

        _CLASS.onClickHandler( event.currentTarget )
    });

    !!SOUND_PLAYER_SLIDER && new SOUND_PLAYER_SLIDER({
        sliderContainer     : playerSkeleton.find('.volume').eq(0),
        sliderHolder        : playerSkeleton.find('.volume .outer').eq(0),
        min                 : 0,
        max                 : 100,
        slideOnMove         : true,
        callback            : function( value ){
            !!_CLASS.activeData && _CLASS.activeData.sound.setVolume( value );
        }
    });

    !!SOUND_PLAYER_SLIDER && new SOUND_PLAYER_SLIDER({
        sliderContainer     : playerSkeleton.find('.timeline'),
        sliderHolder        : playerSkeleton.find('.timeline .outer'),
        min                 : 0,
        max                 : 100,
        slideOnMove         : false,
        callback            : function( value ){

            if( !_CLASS.activeData ) return;

            _CLASS.activeData.sound.setPosition( _CLASS.activeData.sound.duration*value/100 );

            if( !_CLASS.activeTrack ) return;

            var current = _CLASS.calculateEstimateDuration( _CLASS.activeData.sound );

            _CLASS.activeTrack.find( '.duration').html( current[0] + '&#58;' + current[1] );
        }
    });
};

LS_SOUND_PLAYER.prototype.onClickHandler = function( _ELEM ){

        var $activeParent   = $( '.player.active' ),
            $activeChild    = null,
            $track          = $( _ELEM ),
            $currentParent  = $track.parents( '.player' ).eq(0),
            data            = $track.data(),
            isPlaying       = $track.is('.playing'),
            isActive        = $track.is('.active'),
            isLoading       = $track.is('.loading'),
            isPaused        = $track.is('.paused');

        if( isActive ){
            if( isLoading )
                if( isPaused ){
                    $track.removeClass( 'paused' );
                }else{
                    $track.addClass( 'paused' );
                }
            else if( isPlaying ){
                $track.removeClass( 'playing' ).addClass( 'paused' );
                data.sound.pause();
            }else if( isPaused ){
                $track.removeClass( 'paused' ).addClass( 'playing' );
                data.sound.play();
            }
        }else {
            // Likespot.ru: [ Because of stream posts behavior. ]
            $activeParent = ( !$activeParent.length ) ? $currentParent : $activeParent.eq(0);
            $activeParent.removeClass( 'active' );
            $currentParent.addClass( 'active' );
            $activeChild  = $activeParent.find( '.tracks .active' );
            if( $activeChild.length ){
                this.activeTrack = $activeChild.eq(0);
                this.activeData  = $activeChild.eq(0).data();
            }

            if( !!this.activeTrack && !!this.activeData ){
                this.activeTrack.removeClass( 'active playing paused loading' );
                if( this.activeData.sound.readyState == 3 )
                    this.activeData.sound.stop();
                if( this.activeData.sound.readyState == 1 )
                    this.activeData.sound.unload();
            }

            $track.addClass( 'active' );
            this.activeData  = data;
            this.activeTrack = $track;
            // Trigger sound load.
            if( data.sound.readyState == 0 ){
                $track.addClass( 'loading' );

                this.outerTimeline.css({ 'width': 0 });
                data.sound.load({
                    volume:50,
                    onload : function(){

                        $track.removeClass( 'loading' );

                        // Error processing.
                        if( this.readyState != 3 )
                            return;

                        if( $track.hasClass( 'active' ) ){

                            this.setPosition( 0 );

                            if( $track.hasClass( 'paused' ) ){
                                this.pause();
                            }else{
                                $track.removeClass( 'paused' ).addClass( 'playing' );
                                this.play();
                            }
                        }


                    }
                });
                // Trigger sound play.
            }else if( data.sound.readyState == 3 ){
                data.sound.setPosition( 0 );
                $track.addClass( 'playing' );
                data.sound.play();
            }
        }
};

LS_SOUND_PLAYER.prototype.nextTrack = function(){

    this.activeData.sound.stop();

    if ( ! this.activeTrack.next().click().length ){
        $('.player.active .tracks li:first').click();
    }
};

LS_SOUND_PLAYER.prototype.prevTrack = function(){

    this.activeData.sound.stop();

    if ( ! this.activeTrack.prev().click().length ){
        $('.player.active .tracks li:last').click();
    }
};

