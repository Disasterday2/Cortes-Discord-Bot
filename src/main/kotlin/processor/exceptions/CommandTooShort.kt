package processor.exceptions

class CommandTooShort(private val command: String) :
    Exception("Missing input on command: `$command`. For more information use `!help $command`")