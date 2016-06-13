use strict;
use warnings;
use AnyEvent::Socket;
use AnyEvent::Handle;
use Protocol::TLS::Client;
 
# openssl s_server -accept 4443 -cert test.crt -key test.key -debug
# openssl .key .cert *12345
# localhost 'xampp'=127.0.0.1 tcp/udp port=4443
 
my $client = Protocol::TLS::Client->new( version => 'TLSv12', @ARGV );
 
my $cv = AE::cv;
 
tcp_connect '127.0.0.1', 4443, sub {
    my $fh = shift or do {
        warn "error: $!\n";
        $cv->send;
        return;
    };
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
            print "Bye-bye\n";
            $cv->send;
        },
    );
 
    my $con = $client->new_connection(
        '127.0.0.1',
        on_handshake_finish => sub {
            my ($tls) = @_;
	    my $uinput = <STDIN>;
            $tls->send($uinput);
        },
        on_data => sub {
            my ( $tls, $data ) = @_;
            print "received echo\n" if $data =~ /test/;
            print "Server Replied: $data";
            $tls->close;
        }
    );
 
    while ( my $record = $con->next_record ) {
        $h->push_write($record);
    }
 
    $h->on_read(
        sub {
            my $handle = shift;
            $con->feed( $handle->{rbuf} );
            $handle->{rbuf} = '';
            while ( my $record = $con->next_record ) {
                $handle->push_write($record);
            }
 
            # Terminate connection if all done
            $handle->push_shutdown if $con->shutdown;
            ();
        }
    );
    ();
};
 
$cv->recv;
