module Filters exposing (..)


type alias Filters =
    List Filter


type Filter
    = ComponentNameEquals String
    | Journeys


empty =
    []


filterBy filter filters =
    [ filter ]
