module Helpers exposing (debugDecoder)

import Dict exposing (Dict)
import Json.Decode as Decode exposing (Decoder)


parseQuery : String -> Dict String String
parseQuery query =
    query
        |> String.split "&"
        |> List.map (String.split "=")
        |> List.foldl parseHelper Dict.empty


parseHelper : List String -> Dict String String -> Dict String String
parseHelper next acc =
    case next of
        key :: val :: [] ->
            Dict.insert key val acc

        _ ->
            acc


debugDecoder : Decoder a -> Decoder a
debugDecoder realDecoder =
    Decode.value
        |> Decode.andThen
            (\event ->
                case Decode.decodeValue realDecoder event of
                    Ok decoded ->
                        Decode.succeed decoded

                    Err error ->
                        error
                            |> Decode.errorToString
                            |> Debug.log "decoding error"
                            |> Decode.fail
            )
