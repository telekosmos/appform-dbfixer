#!/bin/sh


#
# a simple way to parse shell script arguments
# 
# please edit and use to your hearts content
# 


ENVIRONMENT="dev"
DB_PATH="/data/db"

function usage()
{
    echo "if this was a real script you would see something useful here"
    echo "(param was $param)"
    echo "./simple_args_parsing.sh"
    echo "\t-h --help"
    echo "\t--environment=$ENVIRONMENT"
    echo "\t--db-path=$DB_PATH"
    echo ""
}


function usageparam()
{
    echo "if this was a real script you would see something useful here"
    echo "(param was $1)"
    echo "./simple_args_parsing.sh"
    echo "\t-h --help"
    echo "\t--environment=$ENVIRONMENT"
    echo "\t--db-path=$DB_PATH"
    echo ""
}


while [ "$1" != "" ]; do
    PARAM=`echo $1 | awk -F= '{print $1}'`
#    VALUE=`echo $1 | awk -F= '{print $2}'`
		VALUE=`echo $2 | awk -F= '{print $1}'`
    case $PARAM in
        -h | --help)
            usage
            exit
            ;;
				-ax) echo "value $VALUE"
						usageparam $VALUE
						exit
						;;
        --environment)
            ENVIRONMENT=$VALUE
            ;;
        --db-path)
            DB_PATH=$VALUE
            ;;
        *)
            echo "ERROR: unknown parameter \"$PARAM\""
            usage
            exit 1
            ;;
    esac
    shift
		shift # coge de 2 en 2
done


echo "ENVIRONMENT is $ENVIRONMENT";
echo "DB_PATH is $DB_PATH";