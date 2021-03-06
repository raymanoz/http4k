package org.http4k.serverless

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import org.http4k.core.HttpHandler
import org.http4k.core.Response

/**
 * This is the main entry point for lambda invocations using the V2 payload format.
 * It uses the local environment to instantiate the HttpHandler which can be used
 * for further invocations.
 */
abstract class ApiGatewayV2LambdaFunction(appLoader: AppLoaderWithContexts)
    : AwsLambdaFunction<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>(ApiGatewayV2AwsHttpAdapter, appLoader) {
    constructor(input: AppLoader) : this(AppLoaderWithContexts { env, _ -> input(env) })
    constructor(input: HttpHandler) : this(AppLoader { input })

    override fun handleRequest(req: APIGatewayV2HTTPEvent, ctx: Context) = handle(req, ctx)
}

internal object ApiGatewayV2AwsHttpAdapter : AwsHttpAdapter<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    override fun invoke(req: APIGatewayV2HTTPEvent) =
        RequestContent(req.rawPath, req.queryStringParameters, req.body, req.isBase64Encoded, req.requestContext.http.method, req.headers).asHttp4k()

    override fun invoke(req: Response) = APIGatewayV2HTTPResponse().also {
        it.statusCode = req.status.code
        it.multiValueHeaders = req.headers.groupBy { it.first }.mapValues { it.value.map { it.second } }.toMap()
        it.body = req.bodyString()
    }
}
