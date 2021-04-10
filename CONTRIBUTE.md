# Actions

## Update React Dependencies

```shell script
npm update --force
npm audit fix --force
```


## Pack for manual install (in official Flipper app)

```shell script
yarn pack
```

## Dynamic load of plugin

do not forget to add path of under developpement plugin in config.json file

https://fbflipper.com/docs/extending/loading-custom-plugins


## Start flipper from source

```shell script
git pull
yarn
yarn start
```
