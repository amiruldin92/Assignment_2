use strict;
use warnings;
use AnyEvent::Socket;
use AnyEvent::Handle;
use Protocol::TLS::Server;
 
# openssl s_client -connect 127.0.0.1:4443 -cipher NULL-SHA -debug
# openssl .key .cert *12345
# localhost 'xampp'=127.0.0.1 tcp/udp port=4443
 
my $cv = AE::cv;
 
my $server = Protocol::TLS::Server->new(
    version   => 'TLSv12',
    cert_file => './server.crt',
    key_file  => './server.key',
);
 
tcp_server undef, 4443, sub {
    my ( $fh, $host, $port ) = @_ or do {
        warn "Client error \n";
        $cv->send;
        return;
    };
 
    print "Connected $host:$port\n";
 
    my $con = $server->new_connection(
        on_handshake_finish => sub {
            my ($tls) = @_;
        },
        on_data => sub {
            my ( $tls, $data ) = @_;
	    print "input by user: $data";
            $tls->send("hi, $data");
            $tls->close;
        }
    );
 
    my $h;
    $h = AnyEvent::Handle->new(
        fh       => $fh,
        on_error => sub {
            $_[0]->destroy;
            warn "connection error\n";
            $cv->send;
        },
        on_eof => sub {
            $h->destroy;
            print "Connection closed\nWaiting for new connection\n";
        },
    );
    $h->on_read(
        sub {
            my $handle = shift;
            $con->feed( $handle->{rbuf} );
            $handle->{rbuf} = '';
            while ( my $record = $con->next_record ) {
                $handle->push_write($record);
            }
 
            $handle->push_shutdown if $con->shutdown;
            ();
        }
    );
    ();
};
$cv->recv;
