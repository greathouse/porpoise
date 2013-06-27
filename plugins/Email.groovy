@GrabConfig(systemClassLoader=true)
@Grapes(
	@Grab(group='javax.mail', module='mail', version='1.4')
)

import org.apache.commons.cli.Option

def cli = new CliBuilder(usage: "${this.class.simpleName}.groovy")
cli.with {
	S (longOpt: 'server', args: 1, required: true, 'SMTP server address')
	p (longOpt: 'port', args: 1, 'SMTP server port (Default 25)')
	f (longOpt: 'from', args: 1, required: true, 'Email address for the "from" user')
	t (longOpt: 'to', args: Option.UNLIMITED_VALUES, required: true, valueSeparator: ',', 'Comma separated list of "to" addresses')
	s (longOpt: 'subject', args: 1, required: true, 'Subject of email')
	m (longOpt: 'message', args: 1, required: false, 'Additional Message')
	c (longOpt: 'applied-changesets', args: Option.UNLIMITED_VALUES, valueSeparator: ',', 'Comma separated list of changests')
}


def opts = cli.parse(args)

def server = opts.S
def port = opts.p ?: 25
def f = opts.f
def t = opts.ts
def sub = opts.s
def msg = opts.m ?: ""
def changesets = opts.cs ?: []
println changesets
println opts.cs
println opts.c

msg += "\n\n\nChangesets Applied"
changesets.each { msg += '\n\t' + it }

def ant = new AntBuilder()
ant.mail(mailhost:server, mailport:port,
         subject:sub){
    from(address:f)
	t.each { to(address:it) }
    message(msg)
}