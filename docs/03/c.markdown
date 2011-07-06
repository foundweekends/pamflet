Git Hooks
---------

Git hooks turn Pamflet from a cute toy into digital printing
press. Set them up on a server under your control (you do have one,
yes?) and have your published documentation updated every time you
push.

Assuming your have a `--bare` git repository on your server, it should
already have a `hooks/` subdirectory. In `hooks/`, create or edit a
file `post-update` that is similar to the following:

```sh
#!/bin/sh
HOME=/home/me
DOCB=\$HOME/app/my_doc_build
PUB=\$HOME/app/my_pub
cd \$DOCB
env -i git pull
cd -
\$HOME/bin/pf \$DOCB/docs \$PUB
```

`DOCB` is a clone of the same repo, but with a working tree. `PUB` is the
directory where the finished `html` and other files will be
placed. `HOME` is your home directory, because git runs the hook in a
weird environment and it's best to be explicit.

Set all that up, and make the hook script executable *or it will not
run and you'll be very confused for a while*. Also, make sure that
`~/bin/pf` runs normally on the server.

When you push to the bare repo from elsewhere, you'll see the normal
git output followed by any errors from Pamflet, or else it will just
work. (And yes, this is some pretty lame scripting. Fork this pamflet,
that's what it's here for.)
