package processor.utilities

import org.apache.logging.log4j.Logger

/**
 * Extension function to any Logger Object, that will print out info before a Command started
 *
 * @param parameters the list of parameters that were given to the Command
 */
fun Logger.setup(parameters: List<String>) {
    this.info(
        "${this.name} was called!\t"
                + "Parameters: $parameters"
    )
}