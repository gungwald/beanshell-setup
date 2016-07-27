#!perl
# 
# From http://www.perlmonks.org/?node_id=482992
#
use strict;
use warnings;
use Carp 'croak';

# Win32::Process::Info and Win32::GuiTest can be installed from ppm.

use Win32::Process::Info;

use Win32::GuiTest qw( GetForegroundWindow SetForegroundWindow FindWindowLike SendKeys GetCursorPos GetWindowText );

# Enables debugging output whenever the program is run from a terminal.
# For normal use, run this with wperl.exe
use constant DEBUG => -t STDOUT;

# I like emacs, yes I do! I like emacs, how about you?
use constant KEY_SEQUENCE => "^g";
use constant PUTTY => qr/ - PuTTY$/;
use constant REPEATING_SIGNATURE => qr/^((?>\d+)(?>(?:,(?>\d+))+))(?>(?: \1){59,59})$/;

# Generated from YAPE::Regex::Explain:
# The regular expression:
# 
# ^((?>\d+)(?>(?:,(?>\d+))+))(?>(?: \1){59,59})$
# 
# matches as follows:
# 
# NODE                     EXPLANATION
# --------------------------------------------------------------------
# ^                        the beginning of the string
# --------------------------------------------------------------------
# (                        group and capture to \1:
# --------------------------------------------------------------------
#   (?>                      match (and do not backtrack afterwards):
# --------------------------------------------------------------------
#     \d+                      digits (0-9) (1 or more times
#                              (matching the most amount possible))
# --------------------------------------------------------------------
#   )                        end of look-ahead
# --------------------------------------------------------------------
#   (?>                      match (and do not backtrack afterwards):
# --------------------------------------------------------------------
#     (?:                      group, but do not capture (1 or more
#                              times (matching the most amount
#                              possible)):
# --------------------------------------------------------------------
#       ,                        ','
# --------------------------------------------------------------------
#       (?>                      match (and do not backtrack
#                                afterwards):
# --------------------------------------------------------------------
#         \d+                      digits (0-9) (1 or more times
#                                  (matching the most amount
#                                  possible))
# --------------------------------------------------------------------
#       )                        end of look-ahead
# --------------------------------------------------------------------
#     )+                       end of grouping
# --------------------------------------------------------------------
#   )                        end of look-ahead
# --------------------------------------------------------------------
# )                        end of \1
# --------------------------------------------------------------------
# (?>                      match (and do not backtrack afterwards):
# --------------------------------------------------------------------
#   (?:                      group, but do not capture (between 59
#                            and 59 times (matching the most amount
#                            possible)):
# --------------------------------------------------------------------
#                              ' '
# --------------------------------------------------------------------
#     \1                       what was matched by capture \1
# --------------------------------------------------------------------
#   ){59,59}                 end of grouping
# --------------------------------------------------------------------
# )                        end of look-ahead
# --------------------------------------------------------------------
# $                        before an optional \n, and the end of the
#                          string
# --------------------------------------------------------------------

# If we are started and this program is already running, die.
LockOrDie();

my $lastfrob = time;
my @snapshots;
while ( 1 ) {
    # Don't even bother watching for inactivity if there is no PuTTY a round to frongle.
    WaitWindowLike( undef, PUTTY );
    
    # A queue, one minute long, of snapshots of system state.
    push @snapshots, join( ",",
                           # Something non-numeric snuck in, I think.
                           grep /^\d+$/,
                           GetForegroundWindow(), GetCursorPos(), 
                           FindWindowLike() );
    shift @snapshots if @snapshots > 60;
    
    my $elapsed = time - $lastfrob;
    my $frotz = "@snapshots" =~ REPEATING_SIGNATURE;
    print "$elapsed $frotz\n"
      if DEBUG;
    if ( $elapsed >= 30 and $frotz ) {
        frobnicate_putty();
        $lastfrob = time;
    }
    
    sleep 1;
}

sub WaitWindowLike {
    while ( 1 ) {
        return if FindWindowLike( @_ );
        sleep 60;
    }
}

sub frobnicate_putty {
    my $active = GetForegroundWindow();
    my @windows = FindWindowLike( undef, PUTTY );
    print "Frobbing \@ @{[scalar time]}\n" if DEBUG and @windows;
    for my $window ( @windows ) {
        print "   $window: " . GetWindowText( $window ) . "\n"
          if DEBUG;
        
        # This is utterly obnoxious. Yuck.
        SetForegroundWindow( $window );
        
        SendKeys( KEY_SEQUENCE );
    }
    
    SetForegroundWindow( $active );
}

sub LockOrDie {
    my ($vol, $dirs, $script) = File::Spec->splitpath($0);                      # WLC
    my $lock_file = File::Spec->catfile(File::Spec->tmpdir(), "$script.lck");   # WLC
    my ( $lock_pid )
      = grep /^\d+$/,
        eval { do { local @ARGV = $lock_file;
                    <> } };
    
    my ( $proc )
      = grep { $_->{ProcessId} == $lock_pid
                 and $_->{Name} =~ /perl/i }
        Win32::Process::Info->new->GetProcInfo;
    croak "$0 appears to be already running as $proc->{ProcessId}."
      if $proc;
    
    open my $fh, ">", $lock_file
      or croak "Can't open $lock_file for writing: $^E";
    print $fh "$$\n"
      or croak "Can't write to $lock_file for writing: $^E";
    close $fh
      or croak "Can't close and flush $lock_file after writing: $^E";
    
    return 1;
}

