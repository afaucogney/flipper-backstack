import React, { useState } from 'react';
import {PluginClient, usePlugin, createState, useValue, Layout} from 'flipper-plugin';
import {ManagedDataInspector, DetailSidebar} from 'flipper';
import {Typography, Select, Timeline, Spin} from "antd";

type Data = {
  id: string;
  name?: string;
  lifecycle?: string;
  type?: string;
};

type Event = {
  id: string;
  name?: string;
  lifeCycle: string;
  type?: string;
  timestamp: String;
};

type Events = {
  newData: Data;
  newEvent: Event;
};

// Read more: https://fbflipper.com/docs/tutorial/js-custom#creating-a-first-plugin
// API: https://fbflipper.com/docs/extending/flipper-plugin#pluginclient
export function plugin(client: PluginClient<Events, {}>) {
  const data = createState<Record<string, Data>>({}, {persist: 'data'});
  const event = createState<Record<string, Event>>({}, {persist: 'event'});

  client.onMessage('newData', (newData) => {
    data.update((draft) => {
      draft[newData.id] = newData;
    });
  });

  client.onMessage('newEvent', (newEvent) => {
    event.update((draft) => {
      draft[newEvent.id] = newEvent;
    });
  });

  client.addMenuEntry({
    action: 'clear',
    handler: async () => {
      data.set({});
    },
  });

  return {
    data,
    event
  };
}

// Read more: https://fbflipper.com/docs/tutorial/js-custom#building-a-user-interface-for-the-plugin
// API: https://fbflipper.com/docs/extending/flipper-plugin#react-hooks
export function Component() {
  const instance = usePlugin(plugin);
  const data = useValue(instance.data);
  const events = useValue(instance.event);

  const { Option } = Select;

  // FILTERING OPTION
  interface FilterProps {
    label: string;
    value: string;
  }

  // OBJECT FILTERING
  const defaultFullObjectFilters = ['Application','Activities','Fragments','ViewModels','LiveDatas','Jobs','Services','Trash']
  const objectOptions: FilterProps[] = [];
  const [selectedObjectFilters, setSelectedObjectFilters] = useState(defaultFullObjectFilters)

  for (let i=0; i<defaultFullObjectFilters.length; i++){
    objectOptions.push({
      label: defaultFullObjectFilters[i],
      value: defaultFullObjectFilters[i],
    });
  }

  function handleObjectFilterChange(value) {
    setSelectedObjectFilters(value)
  }

  // EVENT FILTERING
  const defaultFullEventFilters = ['Created','Started','Paused','Attached','ViewCreated','SaveInstanceState']
  const eventOptions: FilterProps[] = [];
  const [selectedEventFilters, setSelectedEventFilters] = useState(defaultFullEventFilters)

  for (let i=0; i<defaultFullEventFilters.length; i++){
    eventOptions.push({
      label: defaultFullEventFilters[i],
      value: defaultFullEventFilters[i],
    });
  }

  function handleEventFilterChange(value) {
    setSelectedEventFilters(value)
  }

  // ROOT VIEW
  if (Object.keys(data).length < 1) {
    return (
      <Layout.ScrollContainer>
        {renderLoading()}
      </Layout.ScrollContainer>
    );
  } else {
    return (
      <>
        <Typography.Title level={4}>App stack structure</Typography.Title>
        {renderObjectFilters()}
        <Layout.ScrollContainer>{renderStackTree(data)}</Layout.ScrollContainer>
        <DetailSidebar>{renderSidebar(events)}</DetailSidebar>
      </>            
    );
  }

  // LOADING VIEW
  function renderLoading() {
    return (
      <div style={{
          position: 'absolute', left: '50%', top: '50%',
          transform: 'translate(-50%, -50%)'
        }}
      >
        <Spin size="large"/> Navigate wihtin the app to update Flipper
      </div>
    );
  }

  // LEFT VIEW
  function renderStackTree(data : Record<string,Data>) {
    return (
      <ManagedDataInspector data={data} expandRoot={true} />
    );
  }

  // RIGHT VIEW
  function renderSidebar(events: unknown) {
    if (events == undefined || Object.keys(events).length < 1) {
      return (
        renderLoading()
      );
    } else {
      return (
        <>
        <Typography.Title level={4}>Event log</Typography.Title>
        {renderEventFilters()}
        <Layout.Container gap pad>
          <Timeline mode="right">{renderTimeLineItems(Object.values(events)[0])}</Timeline>
        </Layout.Container>
        </>
      );
    }
  }

  // EVENT FILTER VIEW
  function renderEventFilters() {
    return (
      <Select
        mode="multiple"
        allowClear
        style={{ width: "100%" }}
        value={selectedEventFilters}
        options={eventOptions}
        placeholder="Please select"
        onChange={handleEventFilterChange}
      />
    );
  }

  // TIMELINE ITEMS VIEW
  function renderTimeLineItems(items : [Object]) {
    console.log("lfe0", items)
    if (items == undefined ) {
      return ""
    } else {
      var result = []
      for (var item of items){
        addEventIfNotFiltered(item, result)
      }
      return result.reverse()
    }
  }

  // EVENT FILTERING
  function addEventIfNotFiltered(event : Event, result: []) {
    switch (event["lifeCycle"]) {
      case "ON_ACTIVITY_CREATED":
      case "ON_ACTIVITY_DESTROYED":
      case "ON_FRAGMENT_ACTIVITY_CREATED":
      case "ON_FRAGMENT_CREATED":        
      case "ON_FRAGMENT_DESTROYED":
        if (selectedEventFilters.includes(defaultFullEventFilters[0])) {
          result.push(renderEventItem(event))
        }
        break
      case "ON_ACTIVITY_STARTED":
      case "ON_ACTIVITY_STOPPED":
      case "ON_FRAGMENT_STARTED":
      case "ON_FRAGMENT_STOPPED":
        if (selectedEventFilters.includes(defaultFullEventFilters[1])) {
          result.push(renderEventItem(event))
        }
        break
      case "ON_ACTIVITY_RESUMED": 
      case "ON_FRAGMENT_RESUMED":
      case "ON_ACTIVITY_PAUSED":
      case "ON_FRAGMENT_PAUSED":
        if (selectedEventFilters.includes(defaultFullEventFilters[2])) {
          result.push(renderEventItem(event))
        }
        break    
      case "ON_FRAGMENT_ATTACHED":
      case "ON_FRAGMENT_DETACHED":
        if (selectedEventFilters.includes(defaultFullEventFilters[3])) {
          result.push(renderEventItem(event))
        }
        break
      case "ON_ACTIVITY_SAVE_INSTANCE_STATE":
      case "ON_FRAGMENT_SAVE_INSTANCE_STATE":
        if (selectedEventFilters.includes(defaultFullEventFilters[5])) {
          result.push(renderEventItem(event))
        }
        break
      case "ON_FRAGMENT_VIEW_CREATED":
      case "ON_FRAGMENT_VIEW_DESTROYED":
        if (selectedEventFilters.includes(defaultFullEventFilters[4])) {
          result.push(renderEventItem(event))
        }
        break
    }
  }

  // TIMELINE ITEM VIEW
  function renderEventItem(event : Event){
    return (
      <Timeline.Item color={getColor(event)} label={event["timestamp"]}>
      {event["lifeCycle"]}
      <p> {event["name"]} ({event["id"]})</p>
      </Timeline.Item>
    );
  }

  // TIMELINE ITEM VIEW COLOR
  function getColor(event : Event){
    switch (event["lifeCycle"]) {
      case "ON_ACTIVITY_CREATED":
      case "ON_ACTIVITY_STARTED" :
      case "ON_ACTIVITY_RESUMED": 
      case "ON_FRAGMENT_ATTACHED":
      case "ON_FRAGMENT_CREATED":
      case "ON_FRAGMENT_VIEW_CREATED":
      case "ON_FRAGMENT_ACTIVITY_CREATED":
      case "ON_FRAGMENT_STARTED":
      case "ON_FRAGMENT_RESUMED":
        return "green"
      case "ON_ACTIVITY_PAUSED":
      case "ON_FRAGMENT_PAUSED":
        return "orange"
      case "ON_ACTIVITY_STOPPED":
      case "ON_ACTIVITY_SAVE_INSTANCE_STATE":
      case "ON_ACTIVITY_DESTROYED":
      case "ON_FRAGMENT_STOPPED":
      case "ON_FRAGMENT_SAVE_INSTANCE_STATE":
      case "ON_FRAGMENT_VIEW_DESTROYED":
      case "ON_FRAGMENT_DESTROYED":
      case "ON_FRAGMENT_DETACHED":
        return "red"
    }
  }

  // EVENT FILTER VIEW
  function renderEventFilters() {
    return (
      <Select
        mode="multiple"
        allowClear
        style={{ width: "100%" }}
        value={selectedEventFilters}
        options={eventOptions}
        placeholder="Please select"
        onChange={handleEventFilterChange}
      />
    );
  }

  // OBJECT FILTER VIEW
  function renderObjectFilters() {
    return (
      <Select
        mode="multiple"
        allowClear
        style={{ width: "100%" }}
        value={selectedObjectFilters}
        options={objectOptions}
        placeholder="Please select"
        onChange={handleObjectFilterChange}
      />
    );
  }
}
